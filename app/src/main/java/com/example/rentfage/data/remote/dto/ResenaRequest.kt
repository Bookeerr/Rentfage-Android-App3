package com.example.rentfage.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * DTO para crear una reseña general (no de una propiedad específica).
 * Solo requiere usuarioId y comentario.
 * idPropiedad y calificacion son opcionales (null para reseñas generales).
 */
data class ResenaRequest(
    @SerializedName("usuarioId")
    val usuarioId: String,        // REQUERIDO: ID del usuario como String
    
    @SerializedName("idPropiedad")
    val idPropiedad: Long? = null,     // OPCIONAL: null para reseñas generales
    
    @SerializedName("calificacion")
    val calificacion: Int? = null,     // OPCIONAL: null si no hay puntaje
    
    val comentario: String              // REQUERIDO: el texto del comentario
)