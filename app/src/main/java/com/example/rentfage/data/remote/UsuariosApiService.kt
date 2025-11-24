package com.example.rentfage.data.remote

import com.example.rentfage.data.remote.dto.UserDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Endpoints del microservicio de usuarios (login/registro).
 */
interface UsuariosApiService {

    @GET("usuarios")
    suspend fun obtenerUsuarios(): Response<List<UserDto>>

    @GET("usuarios/{id}")
    suspend fun obtenerUsuarioPorId(
        @Path("id") id: Long
    ): Response<UserDto>

    @POST("usuarios/login")
    suspend fun login(
        @Query("email") email: String,
        @Query("contrasena") contrasena: String
    ): Response<String>

    @POST("usuarios/register")
    suspend fun register(
        @Body usuario: UserDto
    ): Response<UserDto>
}

