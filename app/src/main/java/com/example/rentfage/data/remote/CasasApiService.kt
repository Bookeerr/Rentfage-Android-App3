package com.example.rentfage.data.remote

import com.example.rentfage.data.remote.dto.CasaDto
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path

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

    // Para actualizar los datos de una propiedad (texto)
    @PUT("propiedades/{id}")
    suspend fun actualizarCasa(
        @Path("id") id: Long,
        @Body casa: CasaDto
    ): Response<CasaDto>

    // Para actualizar la foto de una propiedad existente
    @Multipart
    @PATCH("propiedades/{id}/foto")
    suspend fun actualizarFotoPropiedad(
        @Path("id") id: Long,
        @Part imagen: MultipartBody.Part
    ): Response<Unit>
    
    // NUEVO: Para eliminar una propiedad
    @DELETE("propiedades/{id}")
    suspend fun eliminarPropiedad(
        @Path("id") id: Long
    ): Response<Unit>
}