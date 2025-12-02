package com.example.rentfage.data.remote

import com.example.rentfage.data.remote.dto.CasaDto
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface CasasApiService {
    // CORREGIDO: Ahora devuelve Response<...> y se llama obtenerPropiedades
    @GET("propiedades") 
    suspend fun obtenerPropiedades(): Response<List<CasaDto>>

    // Para el admin: Crear una casa (Multipart para subir foto + datos)
    @Multipart
    @POST("propiedades")
    suspend fun crearCasa(
        @Part imagen: MultipartBody.Part,
        @Part("propiedad") propiedad: RequestBody
    ): Response<CasaDto>
}