package com.example.rentfage

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.rentfage.data.local.dao.UserDao
import com.example.rentfage.data.local.database.AppDatabase
import com.example.rentfage.data.local.entity.UserEntity
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class PruebasDeBaseDeDatos {
    private lateinit var userDao: UserDao
    private lateinit var db: AppDatabase

    @Before
    fun crearBaseDeDatos() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        // Usamos una base de datos en memoria porque la información almacenada aquí desaparece cuando se cierra el proceso.
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        userDao = db.userDao()
    }

    @After
    @Throws(IOException::class)
    fun cerrarBaseDeDatos() {
        db.close()
    }

    // TEST 1: Insertar y Leer
    @Test
    @Throws(Exception::class)
    fun escribirUsuarioYLeerEnLista() = runBlocking {
        val user = UserEntity(
            id = 1,
            name = "Alumno Test",
            email = "alumno@duoc.cl",
            phone = "12345678",
            pass = "1234",
            role = "USER"
        )
        userDao.insertar(user)
        val byEmail = userDao.getByEmail("alumno@duoc.cl")
        assertEquals(byEmail?.name, "Alumno Test")
    }

    // TEST 2: Insertar y Borrar
    @Test
    @Throws(Exception::class)
    fun insertarYBorrarUsuario() = runBlocking {
        // 1. Insertamos un usuario
        val user = UserEntity(name = "Para Borrar", email = "borrar@test.com", phone = "000", pass = "123", role = "USER")
        userDao.insertar(user)

        // 2. Verificamos que hay 1 usuario
        var count = userDao.count()
        assertEquals(1, count)

        // 3. Borramos todo
        userDao.borrarTodos()

        // 4. Verificamos que ahora hay 0 usuarios
        count = userDao.count()
        assertEquals(0, count)
    }
}