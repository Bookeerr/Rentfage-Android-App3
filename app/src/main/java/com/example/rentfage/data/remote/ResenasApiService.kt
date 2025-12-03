package com.example.rentfage.data.remote

import com.example.rentfage.data.remote.dto.ResenaDto
import com.example.rentfage.data.remote.dto.ResenaRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

/**
 * Endpoints del microservicio de reseñas/soporte.
 */
interface ResenasApiService {

    @GET("resenas")
    suspend fun obtenerResenas(): Response<List<ResenaDto>>

    @GET("resenas/propiedad/{propiedadId}")
    suspend fun obtenerResenasPorPropiedad(
        @Path("propiedadId") propiedadId: Long
    ): Response<List<ResenaDto>>

    @POST("resenas")
    suspend fun crearResena(@Body request: ResenaRequest): Response<String>
}