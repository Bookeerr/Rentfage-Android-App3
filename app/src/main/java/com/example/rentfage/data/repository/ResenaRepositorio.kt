package com.example.rentfage.data.repository

import com.example.rentfage.data.local.dao.ResenaDao
import com.example.rentfage.data.local.entity.ResenaEntidad
import com.example.rentfage.data.remote.RemoteModule
import com.example.rentfage.data.remote.ResenasApiService
import com.example.rentfage.data.remote.dto.ResenaDto
import com.example.rentfage.data.remote.dto.toEntity
import kotlinx.coroutines.flow.Flow
import retrofit2.HttpException

class ResenaRepositorio(
    private val resenaDao: ResenaDao,
    private val resenasApi: ResenasApiService = RemoteModule.resenasApi
) {

    // Propiedad para que el admin vea todas las reseñas
    val todasLasResenas: Flow<List<ResenaEntidad>> = resenaDao.obtenerTodas()

    suspend fun sincronizarResenas(): Result<Unit> =
        runCatching {
            val response = resenasApi.obtenerResenas()
            if (!response.isSuccessful) throw HttpException(response)

            val entities = response.body().orEmpty().map { it.toEntity() }
            if (entities.isNotEmpty()) {
                resenaDao.borrarTodas()
                resenaDao.insertarTodas(entities)
            }
        }

    suspend fun enviarResena(
        userId: Int,
        comentario: String,
        calificacion: Int = 5,
        propiedadId: Long? = null
    ) {
        val payload = propiedadId?.let {
            ResenaDto(
                id = null,
                usuarioId = userId.toString(),
                idPropiedad = it,
                calificacion = calificacion,
                comentario = comentario
            )
        }

        if (payload != null) {
            val response = resenasApi.crearResena(payload)
            if (!response.isSuccessful) throw HttpException(response)
            val saved = response.body()?.toEntity() ?: payload.toEntity()
            resenaDao.insertar(saved)
        } else {
            val resena = ResenaEntidad(
                userId = userId,
                comentario = comentario
            )
            resenaDao.insertar(resena)
        }
    }

    suspend fun obtenerResenaDeUsuario(userId: Int): ResenaEntidad? {
        sincronizarResenas().getOrElse { /* devolvemos caché */ }
        return resenaDao.obtenerResenaPorUserId(userId)
    }
}