package com.example.rentfage.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "resenas")
data class ResenaEntidad(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val userId: Int,
    val propiedadId: Long = 0L,
    val calificacion: Int = 5,
    val comentario: String,
    val fechaCreacion: Long = System.currentTimeMillis()
)