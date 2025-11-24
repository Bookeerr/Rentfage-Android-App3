package com.example.rentfage.data.remote

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.ConcurrentHashMap


object RemoteModule {

    // IP mágica para que el emulador vea tu PC (localhost).
    private const val DEFAULT_BASE_HOST = "http://10.0.2.2"
    
    // CORREGIDO: Añadimos el prefijo "/api/" para coincidir con la estructura estándar de microservicios.
    private const val API_PREFIX = "/api/"

    @Volatile
    private var baseHost: String = DEFAULT_BASE_HOST
    
    // Caché para no crear objetos Retrofit repetidos innecesariamente
    private val retrofitCache = ConcurrentHashMap<Int, Retrofit>()

    // Interceptor para loguear las peticiones/respuestas HTTP y facilitar la depuración.
    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    // Cliente OkHttp compartido
    private val okHttp = OkHttpClient.Builder()
        .addInterceptor(logging)
        .build()

    // Genera la URL completa: http://10.0.2.2:PUERTO/api/
    private fun microserviceUrl(port: Int): String {
        val normalizedBase = baseHost.removeSuffix("/")
        return "$normalizedBase:$port$API_PREFIX"
    }

    private fun retrofitFor(port: Int): Retrofit =
        retrofitCache.getOrPut(port) { buildRetrofit(microserviceUrl(port)) }

    private fun buildRetrofit(baseUrl: String): Retrofit =
        Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttp)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    /**
     * Permite actualizar la IP sin recompilar (útil si cambias de red).
     */
    fun updateBaseHost(newHost: String?): String {
        val sanitized = newHost
            ?.takeIf { it.isNotBlank() }
            ?.trim()
            ?.removeSuffix("/")
            ?.let { host ->
                if (host.startsWith("http://") || host.startsWith("https://")) host else "http://$host"
            }
            ?: DEFAULT_BASE_HOST

        if (sanitized != baseHost) {
            baseHost = sanitized
            retrofitCache.clear()
        }
        return baseHost
    }

    // --- INSTANCIAS DE TUS 4 MICROSERVICIOS ---
    // IMPORTANTE: Verifica que estos puertos coincidan con tu VS Code.

    val usuariosApi: UsuariosApiService
        get() = retrofitFor(8081).create(UsuariosApiService::class.java)

    val casasApi: CasasApiService
        get() = retrofitFor(8082).create(CasasApiService::class.java)

    val resenasApi: ResenasApiService
        get() = retrofitFor(8083).create(ResenasApiService::class.java)

    val comprasApi: ComprasApiService
        get() = retrofitFor(8084).create(ComprasApiService::class.java)
}