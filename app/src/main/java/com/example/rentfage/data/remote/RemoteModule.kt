package com.example.rentfage.data.remote

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.ConcurrentHashMap

object RemoteModule {

    // --- CONFIGURACIÓN DE URL ---
    private const val DEFAULT_BASE_HOST = "https://922kt3d4-XXXX.brs.devtunnels.ms"
    
    // Si quieres volver a usar el emulador con localhost, cambia la línea de arriba por:
    // private const val DEFAULT_BASE_HOST = "http://10.0.2.2"

    // Quitamos el prefijo api para microservicio.
    private const val API_PREFIX = "/"

    @Volatile
    private var baseHost: String = DEFAULT_BASE_HOST
    
    private val retrofitCache = ConcurrentHashMap<Int, Retrofit>()

    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttp = OkHttpClient.Builder()
        .addInterceptor(logging)
        .build()

    // FUNCIÓN MEJORADA: Ahora sabe construir URLs para IP local y para Dev Tunnels.
    private fun microserviceUrl(port: Int): String {
        val normalizedBase = baseHost.removeSuffix("/")
        
        return if (normalizedBase.contains("XXXX")) {
            // Lógica para Dev Tunnels: reemplaza XXXX por el puerto
            normalizedBase.replace("XXXX", port.toString()) + API_PREFIX
        } else {
            // Lógica para IP local: añade el puerto al final
            "$normalizedBase:$port$API_PREFIX"
        }
    }

    private fun retrofitFor(port: Int): Retrofit =
        retrofitCache.getOrPut(port) { buildRetrofit(microserviceUrl(port)) }

    private fun buildRetrofit(baseUrl: String): Retrofit =
        Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttp)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

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

    val usuariosApi: UsuariosApiService
        get() = retrofitFor(8081).create(UsuariosApiService::class.java)

    val casasApi: CasasApiService
        get() = retrofitFor(8082).create(CasasApiService::class.java)

    val resenasApi: ResenasApiService
        get() = retrofitFor(8083).create(ResenasApiService::class.java)

    val comprasApi: ComprasApiService
        get() = retrofitFor(8084).create(ComprasApiService::class.java)
}