package com.example.rentfage.data.remote

import com.example.rentfage.data.remote.dto.CompraDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Endpoints del microservicio de compras/contrataciones.
 */
interface ComprasApiService {

    @GET("compras")
    suspend fun obtenerCompras(): Response<List<CompraDto>>

    @GET("compras/usuario/{usuarioId}")
    suspend fun obtenerComprasPorUsuario(
        @Path("usuarioId") usuarioId: Long
    ): Response<List<CompraDto>>

    @POST("compras")
    suspend fun crearCompra(
        @Query("usuarioId") usuarioId: Long,
        @Query("propiedadId") propiedadId: Long
    ): Response<String>

    @POST("compras/detalle")
    suspend fun crearCompraDetalle(
        @Body compra: CompraDto
    ): Response<CompraDto>

    @POST("compras/actualizar/{idCompra}")
    suspend fun actualizarEstado(
        @Path("idCompra") idCompra: Long,
        @Query("nuevoEstado") nuevoEstado: String
    ): Response<String>
}