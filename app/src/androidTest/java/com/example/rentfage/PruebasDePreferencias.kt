package com.example.rentfage

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.rentfage.data.local.storage.UserPreferences
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PruebasDePreferencias {

    private lateinit var userPreferences: UserPreferences

    @Before
    fun configurar() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        userPreferences = UserPreferences(context)
    }

    @Test
    fun guardarYLeerRolUsuario() = runBlocking {
        // Guardamos un rol de prueba
        userPreferences.saveUserRole("ADMIN_TEST")

        // Leemos el valor guardado
        val rolGuardado = userPreferences.userRole.first()

        //  Verificamos que coincida
        assertEquals("ADMIN_TEST", rolGuardado)
    }

    @Test
    fun guardarYLeerEstadoSesion() = runBlocking {
        // Guardamos que el usuario está logueado
        userPreferences.setLoggedIn(true)

        //  Leemos el estado
        val estaLogueado = userPreferences.isLoggedIn.first()

        //  Verificamos que sea verdadero
        assertTrue(estaLogueado)
    }
}