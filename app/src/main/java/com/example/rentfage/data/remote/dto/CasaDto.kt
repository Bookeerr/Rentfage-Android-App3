package com.example.rentfage.data.remote.dto

import com.example.rentfage.data.local.entity.CasaEntity
import com.example.rentfage.data.remote.RemoteModule
import com.google.gson.annotations.SerializedName
import java.util.Locale

data class CasaDto(
    val id: Long? = null,
    val titulo: String,
    val descripcion: String? = null,
    val direccion: String,
    val precio: Double,
    val habitaciones: Int,
    @SerializedName("banos")
    val banos: Int,
    val area: Double,
    val tipo: String,
    val estado: String,
    @SerializedName("propietario_id")
    val propietarioId: Long
)

fun CasaDto.toEntity(isFavorite: Boolean = false): CasaEntity {
    val priceLabel = String.format(Locale.getDefault(), "S/ %.2f", precio)
    val description = buildString {
        appendLine(titulo)
        descripcion?.takeIf { it.isNotBlank() }?.let {
            appendLine(it)
        }
        append("${habitaciones} hab · ${banos} baños · ${area} m² · $tipo")
    }.trim()

    // CORREGIDO: Usamos RemoteModule para construir la URL de la imagen dinámicamente
    // Esto asegura que use la misma URL base configurada (192.168.1.5, localhost, o Dev Tunnels)
    val imageUrl = if (id != null) {
        RemoteModule.getImageUrl(id, port = 8082)
    } else {
        "" // Si no hay ID, no hay imagen
    }

    return CasaEntity(
        id = id?.toInt() ?: 0,
        price = priceLabel,
        address = direccion,
        details = description,
        imageUri = imageUrl,
        latitude = 0.0,
        longitude = 0.0,
        isFavorite = isFavorite
    )
}