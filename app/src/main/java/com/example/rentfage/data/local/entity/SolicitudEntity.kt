package com.example.rentfage.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "solicitudes")
data class SolicitudEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val usuarioId: Long = 0L,
    val usuarioEmail: String,
    val casaId: Int,
    val fecha: String,
    val estado: String = "Pendiente"
)

