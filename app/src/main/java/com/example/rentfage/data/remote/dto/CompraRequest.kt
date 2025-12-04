package com.example.rentfage.data.remote.dto

/**
 * DTO para crear una compra.
 * El backend espera estos campos en el body del POST.
 */
data class CompraRequest(
    val usuarioId: Long,      // REQUERIDO: ID del usuario
    val propiedadId: Long      // REQUERIDO: ID de la propiedad
)
