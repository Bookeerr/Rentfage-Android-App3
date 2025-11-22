package com.example.rentfage.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.rentfage.data.local.entity.ResenaEntidad
import kotlinx.coroutines.flow.Flow

@Dao
interface ResenaDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(resena: ResenaEntidad)

    @Query("SELECT * FROM resenas WHERE userId = :userId ORDER BY fechaCreacion DESC LIMIT 1")
    suspend fun obtenerResenaPorUserId(userId: Int): ResenaEntidad?

    // Nuevo método para el admin
    @Query("SELECT * FROM resenas ORDER BY fechaCreacion DESC")
    fun obtenerTodas(): Flow<List<ResenaEntidad>>
}