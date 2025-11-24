package com.example.rentfage.data.remote.dto

import com.example.rentfage.data.local.entity.SolicitudEntity
import com.google.gson.annotations.SerializedName

data class CompraDto(
    @SerializedName("id_compra")
    val idCompra: Long? = null,
    @SerializedName("id_usuario")
    val idUsuario: Long,
    @SerializedName("id_propiedad")
    val idPropiedad: Long,
    val monto: Double,
    @SerializedName("fecha_compra")
    val fechaCompra: String? = null,
    val estado: String
)

fun CompraDto.toSolicitudEntity(usuarioEmail: String = ""): SolicitudEntity =
    SolicitudEntity(
        id = idCompra?.toInt() ?: 0,
        usuarioId = idUsuario,
        usuarioEmail = usuarioEmail,
        casaId = idPropiedad.toInt(),
        fecha = fechaCompra ?: "",
        estado = estado
    )

