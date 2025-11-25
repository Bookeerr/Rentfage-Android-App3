package com.example.rentfage.data.repository

import com.example.rentfage.data.local.dao.SolicitudDao
import com.example.rentfage.data.local.dao.UserDao
import com.example.rentfage.data.local.entity.SolicitudEntity
import com.example.rentfage.data.remote.ComprasApiService
import com.example.rentfage.data.remote.RemoteModule
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
            
            // CORRECCIÓN CRÍTICA: Si el servidor dice 404, significa "Lista Vacía", NO error.
            if (response.code() == 404) {
                solicitudDao.borrarPorUsuario(email)
                return@runCatching
            }

            if (!response.isSuccessful) throw HttpException(response)

            val entities = response.body().orEmpty()
                .map { it.toSolicitudEntity(email.ifBlank { "Usuario #${it.idUsuario}" }) }

            // Siempre actualizamos la base de datos local para que sea un espejo del servidor
            if (entities.isNotEmpty()) {
                solicitudDao.borrarPorUsuario(email)
                solicitudDao.insertAll(entities)
            } else {
                // Si el servidor devolvió 200 OK pero lista vacía
                solicitudDao.borrarPorUsuario(email)
            }
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
        // 1. PERSISTENCIA REAL: Enviamos la solicitud al microservicio
        val response = comprasApi.crearCompra(userId, casaId)
        
        // OPCIÓN A IMPLEMENTADA: Ignoramos el 404 al crear (si el servidor devuelve eso por algún motivo)
        // o simplemente si falla, lanzamos excepción, pero el 404 de 'obtener' ya lo manejamos arriba.
        
        if (!response.isSuccessful) {
            // A veces el servidor devuelve 201 con un body raro, o un 200.
            // Si devuelve 404 aquí sería raro, pero lanzamos error.
            throw HttpException(response)
        }

        // 2. VISIBILIDAD INMEDIATA: Insertamos en local
        val fechaActual = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        
        val nuevaSolicitudLocal = SolicitudEntity(
            id = 0, // ID temporal
            usuarioId = userId,
            usuarioEmail = userEmail,
            casaId = casaId.toInt(),
            fecha = fechaActual,
            estado = "Pendiente"
        )
        
        solicitudDao.upsert(nuevaSolicitudLocal)

        // 3. SINCRONIZACIÓN: Intentamos confirmar con el servidor.
        // Aquí es donde el 404 de 'obtenerComprasPorUsuario' podría haber molestado antes.
        // Pero con el arreglo de arriba (en sincronizarSolicitudes), ya no molestará.
        try {
            sincronizarSolicitudes(userEmail, userId)
        } catch (e: Exception) {
            // Si falla la sincro (por ejemplo, el servidor tardó en indexar), no pasa nada.
            // El usuario ya ve su solicitud local.
            e.printStackTrace()
        }
    }

    suspend fun actualizarEstado(idCompra: Long, nuevoEstado: String): Result<Unit> =
        runCatching {
            val response = comprasApi.actualizarEstado(idCompra, nuevoEstado)
            if (!response.isSuccessful) throw HttpException(response)
            solicitudDao.actualizarEstado(idCompra.toInt(), nuevoEstado)
        }
}