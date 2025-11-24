package com.example.rentfage.data.remote

import com.example.rentfage.data.remote.dto.CasaDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface CasasApiService {
    // CORREGIDO: Ahora devuelve Response<...> y se llama obtenerPropiedades
    @GET("propiedades") 
    suspend fun obtenerPropiedades(): Response<List<CasaDto>>

    // Para el admin: Crear una casa
    @POST("propiedades")
    suspend fun crearCasa(@Body casa: CasaDto): Response<CasaDto>
}