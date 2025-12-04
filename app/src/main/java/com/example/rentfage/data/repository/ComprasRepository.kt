package com.example.rentfage.data.repository

import android.util.Log
import com.example.rentfage.data.local.dao.SolicitudDao
import com.example.rentfage.data.local.dao.UserDao
import com.example.rentfage.data.local.entity.SolicitudEntity
import com.example.rentfage.data.remote.ComprasApiService
import com.example.rentfage.data.remote.RemoteModule
import com.example.rentfage.data.remote.dto.CompraRequest
import com.example.rentfage.data.remote.dto.toSolicitudEntity
import kotlinx.coroutines.flow.Flow
import retrofit2.HttpException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ComprasRepository(
    private val solicitudDao: SolicitudDao,
    private val userDao: UserDao,
    private val comprasApi: ComprasApiService = RemoteModule.comprasApi
) {

    fun historialDeUsuario(email: String): Flow<List<SolicitudEntity>> =
        solicitudDao.obtenerPorUsuario(email)

    val todasLasSolicitudes: Flow<List<SolicitudEntity>> = solicitudDao.obtenerTodas()

    suspend fun sincronizarSolicitudes(email: String, userId: Long): Result<Unit> =
        runCatching {
            val response = comprasApi.obtenerComprasPorUsuario(userId)
            
            // CORRECCIÓN CRÍTICA: Si el servidor dice 404, NO borramos las solicitudes locales.
            // Pueden ser solicitudes recién creadas que el servidor aún no ha procesado.
            if (response.code() == 404) {
                // No hacemos nada - mantenemos las solicitudes locales
                return@runCatching
            }

            if (!response.isSuccessful) throw HttpException(response)

            val entities = response.body().orEmpty()
                .map { it.toSolicitudEntity(email.ifBlank { "Usuario #${it.idUsuario}" }) }

            // MERGE INTELIGENTE: Actualizamos/insertamos las del servidor
            // PERO NO BORRAMOS las solicitudes locales - solo las actualizamos si coinciden por ID
            // Las solicitudes locales con IDs que no están en el servidor se mantienen
            if (entities.isNotEmpty()) {
                // Insertamos/actualizamos las del servidor (REPLACE actualiza si existe por ID)
                // Las solicitudes locales con IDs diferentes (como las recién creadas con id=0)
                // se mantienen porque tienen IDs únicos generados por Room
                solicitudDao.insertAll(entities)
            }
            // Si el servidor devolvió 200 OK pero lista vacía, mantenemos las solicitudes locales
            // porque pueden ser nuevas y el servidor aún no las ha procesado
        }

    suspend fun sincronizarTodas(): Result<Unit> =
        runCatching {
            val response = comprasApi.obtenerCompras()
            
            // Manejo de 404 o 204 como lista vacía
            if (response.code() == 404 || response.code() == 204) {
                solicitudDao.borrarTodas()
                return@runCatching
            }

            if (!response.isSuccessful) throw HttpException(response)

            val entities = response.body().orEmpty().map { dto ->
                val email = userDao.getById(dto.idUsuario)?.email ?: "Usuario #${dto.idUsuario}"
                dto.toSolicitudEntity(email)
            }
            solicitudDao.borrarTodas()
            if (entities.isNotEmpty()) {
                solicitudDao.insertAll(entities)
            }
        }

    suspend fun enviarSolicitud(
        userId: Long,
        casaId: Long,
        userEmail: String
    ): Result<Unit> = runCatching {
        // 1. VISIBILIDAD INMEDIATA: Guardamos en local PRIMERO
        // Esto asegura que el usuario vea la solicitud incluso si falla el servidor
        val fechaActual = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        
        val nuevaSolicitudLocal = SolicitudEntity(
            id = 0, // ID temporal (Room generará un ID único con autoGenerate)
            usuarioId = userId,
            usuarioEmail = userEmail, // IMPORTANTE: Usar el email exacto del usuario
            casaId = casaId.toInt(),
            fecha = fechaActual,
            estado = "Pendiente"
        )
        
        // Guardamos localmente ANTES de intentar enviar al servidor
        // Esto hace que aparezca inmediatamente en el historial
        val idGenerado = solicitudDao.upsert(nuevaSolicitudLocal)
        Log.d("ComprasRepository", "Solicitud guardada localmente con ID: $idGenerado, email: $userEmail, casaId: $casaId")
        
        // Pequeño delay para asegurar que Room procese el cambio y el Flow se actualice
        kotlinx.coroutines.delay(300)

        // 2. PERSISTENCIA REAL: Intentamos enviar al microservicio en segundo plano
        // Si falla, la solicitud local se mantiene visible
        try {
            val request = CompraRequest(
                usuarioId = userId,
                propiedadId = casaId
            )
            val response = comprasApi.crearCompra(request)
            
            if (!response.isSuccessful) {
                // Si falla, no lanzamos excepción - la solicitud local ya está guardada
                // El usuario verá su solicitud y podrá intentar sincronizar después
                return@runCatching
            }

            // 3. SINCRONIZACIÓN: Si el servidor aceptó, esperamos un poco y sincronizamos
            // PERO: No borramos las solicitudes locales, solo las actualizamos
            try {
                kotlinx.coroutines.delay(1000) // Esperamos más tiempo para que el servidor procese
                sincronizarSolicitudes(userEmail, userId)
            } catch (e: Exception) {
                // Si falla la sincro, no pasa nada - la solicitud local se mantiene
                e.printStackTrace()
            }
        } catch (e: Exception) {
            // Si hay timeout o cualquier error de red, la solicitud local se mantiene
            // El usuario verá su solicitud y podrá intentar sincronizar después
            e.printStackTrace()
            // No lanzamos la excepción - consideramos éxito porque la solicitud local está guardada
        }
    }

    suspend fun actualizarEstado(idCompra: Long, nuevoEstado: String): Result<Unit> =
        runCatching {
            val response = comprasApi.actualizarEstado(idCompra, nuevoEstado)
            if (!response.isSuccessful) throw HttpException(response)
            solicitudDao.actualizarEstado(idCompra.toInt(), nuevoEstado)
        }
}