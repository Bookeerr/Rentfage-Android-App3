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

    // --- ESCRITURA (CONEXIÓN A MICROSERVICIO) ---
    suspend fun insertarCasa(casa: CasaEntity) {
        // 1. Intentamos enviar al servidor PRIMERO
        try {
            // Limpiamos el precio para que sea un número
            val precioDouble = casa.price.replace("S/", "").replace(" ", "").toDoubleOrNull() ?: 0.0
            
            // Creamos un DTO con los datos que tenemos y valores por defecto para los que faltan
            val nuevoDto = CasaDto(
                titulo = "Casa en ${casa.address}", // Título por defecto
                descripcion = casa.details,
                direccion = casa.address,
                precio = precioDouble,
                habitaciones = 2, // Valor por defecto
                banos = 1,        // Valor por defecto
                area = 60.0,      // Valor por defecto
                tipo = "Casa",    // Valor por defecto
                estado = "Disponible",
                propietarioId = 1 // ID de admin o usuario por defecto
            )

            val response = casasApi.crearCasa(nuevoDto)
            
            if (response.isSuccessful) {
                // Si se guardó en el servidor, sincronizamos para tenerla oficial
                sincronizarCasas()
                return 
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // 2. Si falló el servidor o no hay internet, guardamos en LOCAL
        // (Advertencia: Esto se borrará en la próxima sincronización exitosa si no se subió)
        casaDao.insertar(casa)
    }

    suspend fun actualizarCasa(casa: CasaEntity) {
        casaDao.actualizar(casa)
    }

    suspend fun borrarCasa(casa: CasaEntity) {
        casaDao.borrar(casa)
    }
}