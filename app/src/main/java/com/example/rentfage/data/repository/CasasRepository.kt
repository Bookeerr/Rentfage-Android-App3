package com.example.rentfage.data.repository

import com.example.rentfage.data.local.dao.CasaDao
import com.example.rentfage.data.local.entity.CasaEntity
import com.example.rentfage.data.remote.CasasApiService
import com.example.rentfage.data.remote.RemoteModule
import com.example.rentfage.data.remote.dto.CasaDto
import com.example.rentfage.data.remote.dto.toEntity
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.HttpException
import java.io.File

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

    // --- ESCRITURA (CONEXIÓN A MICROSERVICIO - MULTIPART) ---
    suspend fun insertarCasa(casa: CasaEntity, imageFile: File?) {
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

            val gson = Gson()
            val jsonString = gson.toJson(nuevoDto)
            val jsonPart = jsonString.toRequestBody("application/json".toMediaTypeOrNull())

            val imagePart = if (imageFile != null && imageFile.exists()) {
                val requestFile = imageFile.asRequestBody("image/*".toMediaTypeOrNull())
                MultipartBody.Part.createFormData("imagen", imageFile.name, requestFile)
            } else {
                // Envía una parte vacía si no hay imagen
                val emptyBody = ByteArray(0).toRequestBody("image/*".toMediaTypeOrNull())
                 MultipartBody.Part.createFormData("imagen", "", emptyBody)
            }

            val response = casasApi.crearCasa(imagePart, jsonPart)
            
            if (response.isSuccessful) {
                // Si se guardó en el servidor, sincronizamos para tenerla oficial
                sincronizarCasas()
                return 
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // 2. Si falló el servidor o no hay internet, guardamos en LOCAL
        casaDao.insertar(casa)
    }

    suspend fun actualizarCasa(casa: CasaEntity) {
        casaDao.actualizar(casa)
    }
    
    suspend fun limpiarFavoritosLocales() {
        casaDao.resetFavoritos()
    }

    suspend fun borrarCasa(casa: CasaEntity) {
        casaDao.borrar(casa)
    }
}