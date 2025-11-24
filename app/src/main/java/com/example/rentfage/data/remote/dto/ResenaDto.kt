package com.example.rentfage.data.remote.dto

import com.example.rentfage.data.local.entity.ResenaEntidad
import android.os.Build
import com.google.gson.annotations.SerializedName
import java.time.OffsetDateTime

data class ResenaDto(
    val id: Long? = null,
    @SerializedName("usuarioId")
    val usuarioId: String,
    @SerializedName("idPropiedad")
    val idPropiedad: Long,
    val calificacion: Int,
    val comentario: String,
    @SerializedName("fecha_resena")
    val fechaResena: String? = null
)

fun ResenaDto.toEntity(): ResenaEntidad {
    val timestamp = fechaResena?.let { dateString ->
        runCatching {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                OffsetDateTime.parse(dateString).toInstant().toEpochMilli()
            } else {
                System.currentTimeMillis()
            }
        }.getOrElse { System.currentTimeMillis() }
    } ?: System.currentTimeMillis()

    return ResenaEntidad(
        id = id?.toInt() ?: 0,
        userId = usuarioId.toIntOrNull() ?: 0,
        propiedadId = idPropiedad,
        calificacion = calificacion,
        comentario = comentario,
        fechaCreacion = timestamp
    )
}
