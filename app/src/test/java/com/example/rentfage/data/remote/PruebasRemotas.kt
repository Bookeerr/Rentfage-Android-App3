package com.example.rentfage.data.remote

import com.example.rentfage.data.remote.dto.CasaDto
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class PruebasRemotas {

    private lateinit var mockWebServer: MockWebServer
    private lateinit var apiService: CasasApiService

    @Before
    fun setUp() {
        // Iniciamos el servidor falso
        mockWebServer = MockWebServer()
        mockWebServer.start()

        // Creamos una instancia de Retrofit conectada a nuestro servidor falso
        val retrofit = Retrofit.Builder()
            .baseUrl(mockWebServer.url("/")) // Usamos la URL del servidor falso
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        apiService = retrofit.create(CasasApiService::class.java)
    }

    @After
    fun tearDown() {
        // Apagamos el servidor al terminar
        mockWebServer.shutdown()
    }

    @Test
    fun obtenerPropiedades_parcea_json_correctamente() = runBlocking {
        // 1. Definimos qué responderá el servidor falso (JSON idéntico a tu CasaDto)
        val jsonRespuesta = """
            [
                {
                    "id": 1,
                    "titulo": "Casa Bonita",
                    "direccion": "Calle Falsa 123",
                    "precio": 150000.0,
                    "descripcion": "Una casa muy linda",
                    "habitaciones": 3,
                    "banos": 2,
                    "area": 120.0,
                    "tipo": "Casa",
                    "estado": "Disponible",
                    "propietario_id": 10
                },
                {
                    "id": 2,
                    "titulo": "Departamento Centro",
                    "direccion": "Avenida Siempre Viva",
                    "precio": 200000.0,
                    "descripcion": "Cerca de todo",
                    "habitaciones": 2,
                    "banos": 1,
                    "area": 80.0,
                    "tipo": "Departamento",
                    "estado": "Vendido",
                    "propietario_id": 20
                }
            ]
        """.trimIndent()

        // Encolamos la respuesta en el servidor falso
        mockWebServer.enqueue(MockResponse().setBody(jsonRespuesta).setResponseCode(200))

        // 2. Ejecutamos la llamada a la API
        val respuesta = apiService.obtenerPropiedades()

        // 3. Verificamos que la respuesta sea exitosa y los datos sean correctos
        assert(respuesta.isSuccessful)
        val listaCasas = respuesta.body()
        
        assertEquals(2, listaCasas?.size) // Debe haber 2 casas
        assertEquals("Calle Falsa 123", listaCasas?.get(0)?.direccion) // Verificamos la primera
        assertEquals("Avenida Siempre Viva", listaCasas?.get(1)?.direccion) // Verificamos la segunda
        
        // Verificamos un campo extra para estar seguros
        assertEquals(150000.0, listaCasas?.get(0)?.precio)
    }

    // NUEVO TEST: (POST)
    @Test
    fun crearCasa() = runBlocking {
        // 1. Preparamos la respuesta del servidor (simulamos que nos devuelve la casa creada con un ID nuevo)
        val jsonRespuesta = """
            {
                "id": 100,
                "titulo": "Casa Nueva",
                "direccion": "Calle Nueva 123",
                "precio": 300000.0,
                "habitaciones": 4,
                "banos": 3,
                "area": 200.0,
                "tipo": "Casa",
                "estado": "Disponible",
                "propietario_id": 1
            }
        """.trimIndent()

        // El servidor responderá con un código 201 (Created)
        mockWebServer.enqueue(MockResponse().setBody(jsonRespuesta).setResponseCode(201))

        // 2. Creamos el objeto Kotlin que queremos enviar
        val nuevaCasa = CasaDto(
            titulo = "Casa Nueva",
            direccion = "Calle Nueva 123",
            precio = 300000.0,
            habitaciones = 4,
            banos = 3,
            area = 200.0,
            tipo = "Casa",
            estado = "Disponible",
            propietarioId = 1
        )

        // 3. Llamamos a la API (Simulamos el POST)
        val respuesta = apiService.crearCasa(nuevaCasa)

        // 4. Verificamos que la respuesta sea exitosa
        assert(respuesta.isSuccessful)
        assertEquals(201, respuesta.code()) // Verificamos código HTTP
        
        // Verificamos que el servidor nos devolvió el objeto con el ID generado (100)
        assertEquals(100L, respuesta.body()?.id)
        assertEquals("Casa Nueva", respuesta.body()?.titulo)
        
        // 5. Verificamos que la petición enviada al servidor fuera realmente un POST
        val peticionRecibida = mockWebServer.takeRequest()
        assertEquals("POST", peticionRecibida.method)
        assertEquals("/propiedades", peticionRecibida.path)
    }
}