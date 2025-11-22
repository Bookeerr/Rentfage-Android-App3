package com.example.rentfage.data.repository

import com.example.rentfage.data.local.dao.ResenaDao
import com.example.rentfage.data.local.entity.ResenaEntidad
import kotlinx.coroutines.flow.Flow

class ResenaRepositorio(private val resenaDao: ResenaDao) {
    
    // Propiedad para que el admin vea todas las reseñas
    val todasLasResenas: Flow<List<ResenaEntidad>> = resenaDao.obtenerTodas()

    suspend fun enviarResena(userId: Int, comentario: String) {
        val resena = ResenaEntidad(
            userId = userId,
            comentario = comentario
        )
        resenaDao.insertar(resena)
    }

    suspend fun obtenerResenaDeUsuario(userId: Int): ResenaEntidad? {
        return resenaDao.obtenerResenaPorUserId(userId)
    }
}