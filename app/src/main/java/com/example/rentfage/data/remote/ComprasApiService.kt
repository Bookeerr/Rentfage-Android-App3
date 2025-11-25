package com.example.rentfage.data.remote

import com.example.rentfage.data.remote.dto.CompraDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query


interface ComprasApiService {

    // CORREGIDO: Quitamos "api/" porque el Controller tiene @RequestMapping("/compras")
    @GET("compras")
    suspend fun obtenerCompras(): Response<List<CompraDto>>

    @GET("compras/usuario/{usuarioId}")
    suspend fun obtenerComprasPorUsuario(
        @Path("usuarioId") usuarioId: Long
    ): Response<List<CompraDto>>

    // POST para crear compra (según tu Java: public ResponseEntity<String> crearCompra...)
    @POST("compras")
    suspend fun crearCompra(
        @Query("usuarioId") usuarioId: Long,
        @Query("propiedadId") propiedadId: Long
    ): Response<String>

    // NOTA: En tu Java no veo un @PostMapping("/detalle"), así que probablemente esta no funcione
    // a menos que la tengas definida en otro lado. La mantengo por si acaso.
    @POST("compras/detalle")
    suspend fun crearCompraDetalle(
        @Body compra: CompraDto
    ): Response<CompraDto>

    // CORREGIDO: Tu Java usa @PutMapping, no @PostMapping para actualizar
    @PUT("compras/actualizar/{idCompra}")
    suspend fun actualizarEstado(
        @Path("idCompra") idCompra: Long,
        @Query("nuevoEstado") nuevoEstado: String
    ): Response<String>
    
    // Añado el DELETE por si lo necesitas en el futuro
    @DELETE("compras/eliminar/{idCompra}")
    suspend fun eliminarCompra(
        @Path("idCompra") idCompra: Long
    ): Response<String>
}