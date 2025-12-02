package com.example.rentfage.data.repository

import com.example.rentfage.data.local.dao.SolicitudDao
import com.example.rentfage.data.local.dao.UserDao
import com.example.rentfage.data.local.entity.SolicitudEntity
import com.example.rentfage.data.remote.CompraRequest
import com.example.rentfage.data.remote.ComprasApiService
import com.example.rentfage.data.remote.RemoteModule
import com.example.rentfage.data.remote.dto.toSolicitudEntity
import kotlinx.coroutines.delay
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
            
            if (response.code() == 404) {
                // No hacemos nada, dejamos las solicitudes locales intactas
                return@runCatching
            }

            if (!response.isSuccessful) throw HttpException(response)

            val entities = response.body().orEmpty()
                .map { it.toSolicitudEntity(email.ifBlank { "Usuario #${it.idUsuario}" }) }

            if (entities.isNotEmpty()) {
                solicitudDao.borrarPorUsuario(email)
                solicitudDao.insertAll(entities)
            } else {
                solicitudDao.borrarPorUsuario(email)
            }
        }

    suspend fun sincronizarTodas(): Result<Unit> =
        runCatching {
            val response = comprasApi.obtenerCompras()
            
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
        // 1. Enviamos la solicitud al microservicio
        val request = CompraRequest(userId, casaId)
        val response = comprasApi.crearCompra(request)
        
        if (!response.isSuccessful) {
            throw HttpException(response)
        }

        // 2. Insertamos en local para visibilidad inmediata
        val fechaActual = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val nuevaSolicitudLocal = SolicitudEntity(
            usuarioId = userId,
            usuarioEmail = userEmail,
            casaId = casaId.toInt(),
            fecha = fechaActual,
            estado = "Pendiente",
            tituloPropiedad = null // Se llenará al sincronizar
        )
        solicitudDao.upsert(nuevaSolicitudLocal)
        
        // 3. Sincronizamos después de un delay para dar tiempo al backend
        delay(500) // Esperamos medio segundo
        try {
            sincronizarSolicitudes(userEmail, userId).getOrElse {
                // Si falla (incluyendo 404), no pasa nada. El usuario ya ve su solicitud local.
            }
        } catch (e: Exception) {
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