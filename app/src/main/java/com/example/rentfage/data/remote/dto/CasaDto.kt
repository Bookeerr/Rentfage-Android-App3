package com.example.rentfage.data.remote.dto

import com.example.rentfage.data.local.entity.CasaEntity
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

    // CORREGIDO: Generamos la URL de la imagen dinámicamente
    return CasaEntity(
        id = id?.toInt() ?: 0,
        price = priceLabel,
        address = direccion,
        details = description,
        imageUri = "http://10.0.2.2:8082/propiedades/${id ?: 0}/imagen",
        latitude = 0.0,
        longitude = 0.0,
        isFavorite = isFavorite
    )
}