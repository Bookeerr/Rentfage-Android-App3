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

    suspend fun enviarSolicitud(
        userId: Long,
        casaId: Long,
        userEmail: String
    ): Result<Unit> =
        runCatching {
            val response = comprasApi.crearCompra(userId, casaId)
            if (!response.isSuccessful) throw HttpException(response)
            sincronizarSolicitudes(userEmail, userId)
        }

    suspend fun actualizarEstado(idCompra: Long, nuevoEstado: String): Result<Unit> =
        runCatching {
            val response = comprasApi.actualizarEstado(idCompra, nuevoEstado)
            if (!response.isSuccessful) throw HttpException(response)
            solicitudDao.actualizarEstado(idCompra.toInt(), nuevoEstado)
        }
}

