package com.example.rentfage.data.repository

import com.example.rentfage.data.local.dao.SolicitudDao
import com.example.rentfage.data.local.dao.UserDao
import com.example.rentfage.data.local.entity.SolicitudEntity
import com.example.rentfage.data.remote.ComprasApiService
import com.example.rentfage.data.remote.RemoteModule
import com.example.rentfage.data.remote.dto.toSolicitudEntity
import kotlinx.coroutines.flow.Flow
import retrofit2.HttpException

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
            if (!response.isSuccessful) throw HttpException(response)

            val entities = response.body().orEmpty()
                .map { it.toSolicitudEntity(email.ifBlank { "Usuario #${it.idUsuario}" }) }

            solicitudDao.borrarPorUsuario(email)
            if (entities.isNotEmpty()) {
                solicitudDao.insertAll(entities)
            }
        }

    suspend fun sincronizarTodas(): Result<Unit> =
        runCatching {
            val response = comprasApi.obtenerCompras()
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

    // CORREGIDO: Lógica más simple y directa para crear una solicitud
    suspend fun enviarSolicitud(
        userId: Long,
        casaId: Long,
        userEmail: String
    ): Result<Unit> = runCatching {
        // 1. Enviamos la solicitud para crear la compra.
        // Usaremos `crearCompraDetalle` si el backend lo soporta, ya que es más robusto.
        // Si no, `crearCompra` con IDs también sirve.
        val response = comprasApi.crearCompra(userId, casaId)
        if (!response.isSuccessful) {
            throw HttpException(response)
        }

        // 2. Después de crear, refrescamos la lista COMPLETA desde el servidor.
        // Esto asegura que vemos la nueva solicitud y cualquier cambio de estado.
        sincronizarSolicitudes(userEmail, userId).getOrThrow()
    }

    suspend fun actualizarEstado(idCompra: Long, nuevoEstado: String): Result<Unit> =
        runCatching {
            val response = comprasApi.actualizarEstado(idCompra, nuevoEstado)
            if (!response.isSuccessful) throw HttpException(response)
            solicitudDao.actualizarEstado(idCompra.toInt(), nuevoEstado)
        }
}
