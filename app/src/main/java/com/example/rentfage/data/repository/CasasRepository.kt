package com.example.rentfage.data.repository

import com.example.rentfage.data.local.dao.CasaDao
import com.example.rentfage.data.local.entity.CasaEntity
import com.example.rentfage.data.remote.CasasApiService
import com.example.rentfage.data.remote.RemoteModule
import com.example.rentfage.data.remote.dto.CasaDto
import com.example.rentfage.data.remote.dto.toEntity
import kotlinx.coroutines.flow.Flow
import retrofit2.HttpException

// Repositorio para manejar los datos de las casas.
class CasasRepository(
    private val casaDao: CasaDao,
    private val casasApi: CasasApiService = RemoteModule.casasApi
) {

    // --- LECTURA ---
    val todasLasCasas: Flow<List<CasaEntity>> = casaDao.obtenerTodas()
    val casasFavoritas: Flow<List<CasaEntity>> = casaDao.getFavoritas()

    fun getById(id: Int): Flow<CasaEntity?> = casaDao.getById(id)

    // --- REMOTO ---
    suspend fun sincronizarCasas(): Result<Unit> =
        runCatching {
            val response = casasApi.obtenerPropiedades()
            if (!response.isSuccessful) {
                throw HttpException(response)
            }

            val dtoList: List<CasaDto> = response.body().orEmpty()
            val favorites = casaDao.obtenerIdsFavoritos().toSet()
            val entities = dtoList.map { dto ->
                val entity = dto.toEntity()
                if (favorites.contains(entity.id)) {
                    entity.copy(isFavorite = true)
                } else {
                    entity
                }
            }

            casaDao.borrarTodas()
            casaDao.insertarTodas(entities)
        }

    // --- ESCRITURA LOCAL ---
    suspend fun insertarCasa(casa: CasaEntity) {
        casaDao.insertar(casa)
    }

    suspend fun actualizarCasa(casa: CasaEntity) {
        casaDao.actualizar(casa)
    }

    suspend fun borrarCasa(casa: CasaEntity) {
        casaDao.borrar(casa)
    }
}