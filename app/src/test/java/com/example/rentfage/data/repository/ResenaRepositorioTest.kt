package com.example.rentfage.data.repository

import com.example.rentfage.data.local.dao.ResenaDao
import com.example.rentfage.data.local.entity.ResenaEntidad
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class ResenaRepositorioTest {

    private lateinit var resenaDao: ResenaDao
    private lateinit var repository: ResenaRepositorio

    @Before
    fun setUp() {
        // Preparamos el mock y el repositorio antes de cada test
        resenaDao = mockk(relaxed = true)
        repository = ResenaRepositorio(resenaDao)
    }

    @Test
    fun `todasLasResenas obtiene el flow desde el DAO`() = runBlocking {
        // Arrange: Preparamos datos falsos
        val listaFalsa = listOf(ResenaEntidad(id = 1, userId = 1, comentario = "Test 1"))
        every { resenaDao.obtenerTodas() } returns flowOf(listaFalsa)

        // Act: Re-creamos el repo para que lea la nueva configuración del Flow
        repository = ResenaRepositorio(resenaDao)
        val resultado = repository.todasLasResenas.first()

        // Assert: Comprobamos el resultado
        assertEquals(listaFalsa, resultado)
    }

    @Test
    fun `enviarResena llama al DAO para insertar`() = runBlocking {
        // Arrange: Datos para la nueva reseña
        val userId = 1
        val comentario = "Este es un buen comentario"

        // Act: Llamamos a la función del repositorio
        repository.enviarResena(userId, comentario)

        // Assert: Verificamos que se llamó a `insertar` en el DAO con los datos correctos
        coVerify { resenaDao.insertar(match { it.userId == userId && it.comentario == comentario }) }
    }

    @Test
    fun `obtenerResenaDeUsuario obtiene la reseña correcta del DAO`() = runBlocking {
        // Arrange
        val userId = 1
        val reseñaFalsa = ResenaEntidad(id = 10, userId = userId, comentario = "Mi reseña anterior")
        coEvery { resenaDao.obtenerResenaPorUserId(userId) } returns reseñaFalsa

        // Act
        val resultado = repository.obtenerResenaDeUsuario(userId)

        // Assert
        assertEquals(reseñaFalsa, resultado)
    }

    @Test
    fun `obtenerResenaDeUsuario devuelve null si el DAO no encuentra nada`() = runBlocking {
        // Arrange
        val userId = 99 // Un usuario que no tiene reseña
        coEvery { resenaDao.obtenerResenaPorUserId(userId) } returns null

        // Act
        val resultado = repository.obtenerResenaDeUsuario(userId)

        // Assert
        assertNull(resultado)
    }
}
