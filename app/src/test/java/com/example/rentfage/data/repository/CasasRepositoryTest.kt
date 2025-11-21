package com.example.rentfage.data.repository

import com.example.rentfage.data.local.dao.CasaDao
import com.example.rentfage.data.local.entity.CasaEntity
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class CasasRepositoryTest {

    private lateinit var casaDao: CasaDao
    private lateinit var repository: CasasRepository

    @Before
    fun setup() {
        casaDao = mockk()
        every { casaDao.obtenerTodas() } returns flowOf(emptyList()) 
        every { casaDao.getFavoritas() } returns flowOf(emptyList()) 
        repository = CasasRepository(casaDao)
    }

    //  TESTS DE LECTURA (Equivalente a GET en una API)
    @Test
    fun todasLasCasas_obtiene_datos_del_DAO() = runBlocking {
        val listaFalsa = listOf(CasaEntity(id = 1, price = "100", address = "A", details = "D", imageUri = "U", latitude = 0.0, longitude = 0.0))
        every { casaDao.obtenerTodas() } returns flowOf(listaFalsa)
        repository = CasasRepository(casaDao)
        val resultado = repository.todasLasCasas.first()
        assertEquals(listaFalsa, resultado)
    }

    @Test
    fun casasFavoritas_obtiene_datos_del_DAO() = runBlocking {
        val listaFavoritas = listOf(CasaEntity(id = 2, price = "200", address = "Fav", details = "D", imageUri = "U", latitude = 0.0, longitude = 0.0, isFavorite = true))
        every { casaDao.getFavoritas() } returns flowOf(listaFavoritas)
        repository = CasasRepository(casaDao)
        val resultado = repository.casasFavoritas.first()
        assertEquals(listaFavoritas, resultado)
    }

    @Test
    fun getById_obtiene_casa_especifica() = runBlocking {
        val casa = CasaEntity(id = 1, price = "100", address = "A", details = "D", imageUri = "U", latitude = 0.0, longitude = 0.0)
        every { casaDao.getById(1) } returns flowOf(casa)
        val resultado = repository.getById(1).first()
        assertEquals(casa, resultado)
    }

    //  TEST DE CREACIÓN (Equivalente a POST en una API)
    @Test
    fun insertarCasa_llama_a_DAO_guardar() = runBlocking {
        val casaNueva = CasaEntity(id = 1, price = "200", address = "B", details = "D", imageUri = "U", latitude = 0.0, longitude = 0.0)
        coEvery { casaDao.insertar(any()) } returns Unit
        repository.insertarCasa(casaNueva)
        coVerify { casaDao.insertar(casaNueva) }
    }

    //  TEST DE ACTUALIZACIÓN (Equivalente a PUT en una API)
    @Test
    fun actualizarCasa_llama_a_DAO_actualizar() = runBlocking {
        val casaEditada = CasaEntity(id = 1, price = "300", address = "Editada", details = "D", imageUri = "U", latitude = 0.0, longitude = 0.0)
        coEvery { casaDao.actualizar(any()) } returns Unit
        repository.actualizarCasa(casaEditada)
        coVerify { casaDao.actualizar(casaEditada) }
    }

    //  TEST DE BORRADO (Equivalente a DELETE en una API)
    @Test
    fun borrarCasa_llama_a_DAO_borrar() = runBlocking {
        val casaBorrar = CasaEntity(id = 99, price = "200", address = "B", details = "D", imageUri = "U", latitude = 0.0, longitude = 0.0)
        coEvery { casaDao.borrar(any()) } returns Unit
        repository.borrarCasa(casaBorrar)
        coVerify { casaDao.borrar(casaBorrar) }
    }
}
