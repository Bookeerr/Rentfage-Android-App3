package com.example.rentfage.data.repository

import com.example.rentfage.data.local.dao.ResenaDao
import com.example.rentfage.data.local.entity.ResenaEntidad
import com.example.rentfage.data.remote.RemoteModule
import com.example.rentfage.data.remote.ResenasApiService
import com.example.rentfage.data.remote.dto.ResenaDto
import com.example.rentfage.data.remote.dto.ResenaRequest
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
        // Crear el request con campos opcionales (reseña general)
        val request = ResenaRequest(
            usuarioId = userId.toString(),
            idPropiedad = propiedadId,  // null para reseñas generales
            calificacion = calificacion,  // null si no hay puntaje
            comentario = comentario
        )

        // Siempre enviamos al servidor (incluso si no hay propiedad)
        val response = resenasApi.crearResena(request)
        if (!response.isSuccessful) throw HttpException(response)

        // Guardar en BD local después del éxito
        val resena = ResenaEntidad(
            userId = userId,
            propiedadId = propiedadId ?: 0L,
            calificacion = calificacion,
            comentario = comentario
        )
        resenaDao.insertar(resena)
    }

    suspend fun obtenerResenaDeUsuario(userId: Int): ResenaEntidad? {
        sincronizarResenas().getOrElse { /* devolvemos caché */ }
        return resenaDao.obtenerResenaPorUserId(userId)
    }
}