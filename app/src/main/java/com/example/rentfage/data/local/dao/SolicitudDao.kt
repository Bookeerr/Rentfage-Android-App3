package com.example.rentfage.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.rentfage.data.local.entity.SolicitudEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SolicitudDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(solicitud: SolicitudEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(solicitudes: List<SolicitudEntity>)

    @Query("SELECT * FROM solicitudes WHERE usuarioEmail = :email ORDER BY fecha DESC")
    fun obtenerPorUsuario(email: String): Flow<List<SolicitudEntity>>

    @Query("SELECT * FROM solicitudes ORDER BY fecha DESC")
    fun obtenerTodas(): Flow<List<SolicitudEntity>>

    @Query("UPDATE solicitudes SET estado = :nuevoEstado WHERE id = :id")
    suspend fun actualizarEstado(id: Int, nuevoEstado: String)

    // FUNCIÓN AÑADIDA: El repositorio la necesita para la sincronización
    @Query("DELETE FROM solicitudes WHERE usuarioEmail = :email")
    suspend fun borrarPorUsuario(email: String)

    @Query("DELETE FROM solicitudes")
    suspend fun borrarTodas()
}