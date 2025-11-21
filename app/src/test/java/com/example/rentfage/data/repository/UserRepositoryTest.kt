package com.example.rentfage.data.repository

import com.example.rentfage.data.local.dao.UserDao
import com.example.rentfage.data.local.entity.UserEntity
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class UserRepositoryTest {

    private lateinit var userDao: UserDao
    private lateinit var repository: UserRepository

    @Before
    fun setup() {
        userDao = mockk(relaxed = true)
        repository = UserRepository(userDao)
    }

    // --- TEST LOGIN ---
    @Test
    fun login_retorna_usuario_si_credenciales_son_correctas() = runBlocking {
        val usuario = UserEntity(name = "Test", email = "test@mail.com", phone = "12345678", pass = "Pass123!", role = "USER")
        // En tu codigo real, login llama a getByEmail y luego compara la pass manualmente
        coEvery { userDao.getByEmail("test@mail.com") } returns usuario

        val resultado = repository.login("test@mail.com", "Pass123!")

        assertTrue(resultado.isSuccess)
        assertEquals(usuario, resultado.getOrNull())
    }

    @Test
    fun login_retorna_fallo_si_password_es_incorrecta() = runBlocking {
        val usuario = UserEntity(name = "Test", email = "test@mail.com", phone = "12345678", pass = "Pass123!", role = "USER")
        coEvery { userDao.getByEmail("test@mail.com") } returns usuario

        // Probamos con una contraseña que NO coincide
        val resultado = repository.login("test@mail.com", "MalPass")

        assertTrue(resultado.isFailure)
    }

    @Test
    fun login_retorna_fallo_si_usuario_no_existe() = runBlocking {
        coEvery { userDao.getByEmail("noexiste@mail.com") } returns null

        val resultado = repository.login("noexiste@mail.com", "Cualquiera")

        assertTrue(resultado.isFailure)
    }

    //  TEST REGISTRO
    @Test
    fun register_llama_al_DAO_para_insertar_usuario() = runBlocking {
        // Simulamos que NO existe
        coEvery { userDao.getByEmail("nuevo@mail.com") } returns null
        // Simulamos que insertar devuelve el ID 1
        coEvery { userDao.insertar(any()) } returns 1L
        
        val resultado = repository.register("Nuevo", "nuevo@mail.com", "12345678", "Pass123!")

        assertTrue(resultado.isSuccess)
        assertEquals(1L, resultado.getOrNull())
        
        // Verificamos que se llamo a insertar con los datos correctos
        coVerify { userDao.insertar(match { it.email == "nuevo@mail.com" && it.pass == "Pass123!" }) }
    }

    @Test
    fun register_falla_si_el_email_ya_existe() = runBlocking {
        val usuarioExistente = UserEntity(name = "Test", email = "test@mail.com", phone = "12345678", pass = "Pass123!", role = "USER")
        // Simulamos que YA existe
        coEvery { userDao.getByEmail("test@mail.com") } returns usuarioExistente

        val resultado = repository.register("Otro", "test@mail.com", "12345678", "Pass123!")

        assertTrue(resultado.isFailure)
        // Verificamos que NO se llamo a insertar
        coVerify(exactly = 0) { userDao.insertar(any()) }
    }

    //  TEST OBTENER USUARIO
    @Test
    fun getUserByEmail_retorna_usuario_del_DAO() = runBlocking {
        val usuario = UserEntity(name = "Test", email = "test@mail.com", phone = "12345678", pass = "Pass123!", role = "USER")
        coEvery { userDao.getByEmail("test@mail.com") } returns usuario

        val resultado = repository.getUserByEmail("test@mail.com")

        assertEquals(usuario, resultado)
    }

    //  TEST ACTUALIZAR PERFIL
    @Test
    fun updateProfile_actualiza_datos_si_usuario_existe() = runBlocking {
        val usuarioOriginal = UserEntity(name = "Viejo", email = "test@mail.com", phone = "11111111", pass = "Pass123!", role = "USER")
        coEvery { userDao.getByEmail("test@mail.com") } returns usuarioOriginal

        val resultado = repository.updateProfile("test@mail.com", "Nuevo Nombre", "22222222")

        assertTrue(resultado.isSuccess)
        // Verificamos que se actualizo con los nuevos datos
        coVerify { 
            userDao.updateUser(match { 
                it.name == "Nuevo Nombre" && it.phone == "22222222" 
            }) 
        }
    }

    //  TEST CAMBIAR PASSWORD
    @Test
    fun changePassword_actualiza_password_si_credenciales_correctas() = runBlocking {
        val usuario = UserEntity(name = "Test", email = "test@mail.com", phone = "12345678", pass = "OldPass!", role = "USER")
        coEvery { userDao.getByEmail("test@mail.com") } returns usuario
        
        val resultado = repository.changePassword("test@mail.com", "OldPass!", "NewPass!")

        assertTrue(resultado.isSuccess)
        coVerify { 
            userDao.updateUser(match { it.pass == "NewPass!" }) 
        }
    }
    
    @Test
    fun changePassword_falla_si_password_actual_es_incorrecta() = runBlocking {
        val usuario = UserEntity(name = "Test", email = "test@mail.com", phone = "12345678", pass = "RealPass!", role = "USER")
        coEvery { userDao.getByEmail("test@mail.com") } returns usuario

        // Intentamos con password actual incorrecta
        val resultado = repository.changePassword("test@mail.com", "WrongPass!", "NewPass!")

        assertTrue(resultado.isFailure)
        coVerify(exactly = 0) { userDao.updateUser(any()) }
    }
}
