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
            // CORRECCIÓN: Borramos primero para evitar datos viejos si la sincro falla
            casaDao.borrarTodas()

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
            
            // Volvemos a insertar los datos nuevos
            casaDao.insertarTodas(entities)
        }

    // --- ESCRITURA (CONEXIÓN A MICROSERVICIO - MULTIPART) ---
    suspend fun insertarCasa(casa: CasaEntity, imageFile: File?) {
        // 1. Intentamos enviar al servidor PRIMERO
        try {
            val precioDouble = casa.price.replace("S/", "").replace(" ", "").toDoubleOrNull() ?: 0.0
            
            val nuevoDto = CasaDto(
                titulo = "Casa en ${casa.address}", 
                descripcion = casa.details,
                direccion = casa.address,
                precio = precioDouble,
                habitaciones = 2, 
                banos = 1,        
                area = 60.0,      
                tipo = "Casa",    
                estado = "Disponible",
                propietarioId = 1 
            )

            val gson = Gson()
            val jsonString = gson.toJson(nuevoDto)
            val jsonPart = jsonString.toRequestBody("application/json".toMediaTypeOrNull())

            val imagePart = if (imageFile != null && imageFile.exists()) {
                val requestFile = imageFile.asRequestBody("image/*".toMediaTypeOrNull())
                MultipartBody.Part.createFormData("imagen", imageFile.name, requestFile)
            } else {
                val emptyBody = ByteArray(0).toRequestBody("image/*".toMediaTypeOrNull())
                 MultipartBody.Part.createFormData("imagen", "", emptyBody)
            }

            val response = casasApi.crearCasa(imagePart, jsonPart)
            
            if (response.isSuccessful) {
                sincronizarCasas()
                return 
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // 2. Si falló el servidor, guardamos en LOCAL
        casaDao.insertar(casa)
    }

    suspend fun actualizarCasa(casa: CasaEntity, imageFile: File? = null) {
        // Si hay una nueva imagen, intentamos actualizarla en el servidor
        if (imageFile != null && imageFile.exists() && casa.id > 0) {
            try {
                val requestFile = imageFile.asRequestBody("image/*".toMediaTypeOrNull())
                val imagePart = MultipartBody.Part.createFormData("imagen", imageFile.name, requestFile)
                
                val response = casasApi.actualizarFotoPropiedad(casa.id.toLong(), imagePart)
                if (response.isSuccessful) {
                    // Si la actualización fue exitosa, sincronizamos para obtener la nueva URL
                    sincronizarCasas()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                // Si falla, continuamos con la actualización local
            }
        }
        
        // Actualizamos en la base de datos local
        casaDao.actualizar(casa)
    }
    
    suspend fun limpiarFavoritosLocales() {
        casaDao.resetFavoritos()
    }

    suspend fun borrarCasa(casa: CasaEntity) {
        casaDao.borrar(casa)
    }
}