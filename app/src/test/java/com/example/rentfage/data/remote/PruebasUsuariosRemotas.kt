package com.example.rentfage.data.remote

import com.google.gson.GsonBuilder
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class PruebasUsuariosRemotas {

    private lateinit var mockWebServer: MockWebServer
    private lateinit var usuariosApi: UsuariosApiService

    @Before
    fun setUp() {
        mockWebServer = MockWebServer()
        mockWebServer.start()

        // CORRECCIÓN EXTRA: Configuramos Gson en modo "Lenient" para ser más tolerante
        val gson = GsonBuilder()
            .setLenient()
            .create()

        val retrofit = Retrofit.Builder()
            .baseUrl(mockWebServer.url("/"))
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()

        usuariosApi = retrofit.create(UsuariosApiService::class.java)
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
    }

    @Test
    fun login_envia_credenciales_correctamente() = runBlocking {
        // CORRECCIÓN: El body debe ser un String JSON válido (con comillas escapadas)
        // "Login Exitoso" -> JSON String válido
        mockWebServer.enqueue(MockResponse().setBody("\"Login Exitoso\"").setResponseCode(200))

        // 2. Llamada
        val email = "test@example.com"
        val pass = "123456"
        val respuesta = usuariosApi.login(email, pass)

        // 3. Assert
        assert(respuesta.isSuccessful)
        assertEquals("Login Exitoso", respuesta.body())

        // 4. Verificación de la petición
        val peticion = mockWebServer.takeRequest()
        val url = peticion.requestUrl!!

        assertEquals("POST", peticion.method)
        assertEquals("/usuarios/login", url.encodedPath)
        
        // Verificamos parámetros
        assertEquals("test@example.com", url.queryParameter("email"))
        assertEquals("123456", url.queryParameter("contrasena"))
    }
}