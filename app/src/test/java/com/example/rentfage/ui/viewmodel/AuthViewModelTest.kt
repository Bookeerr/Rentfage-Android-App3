package com.example.rentfage.ui.viewmodel

import android.app.Application
import com.example.rentfage.data.local.entity.UserEntity
import com.example.rentfage.data.local.storage.UserPreferences
import com.example.rentfage.data.repository.UserRepository
import io.mockk.coEvery
import io.mockk.mockk
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowLooper

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class AuthViewModelTest {

    private lateinit var viewModel: AuthViewModel
    private lateinit var repository: UserRepository
    private lateinit var userPreferences: UserPreferences // Mockeamos esto
    private lateinit var application: Application

    @Before
    fun setup() {
        repository = mockk(relaxed = true)
        application = mockk(relaxed = true)
        userPreferences = mockk(relaxed = true) // Creamos el mock de preferencias
        
        // Inyectamos el mock de preferencias en el ViewModel
        viewModel = AuthViewModel(application, repository, userPreferences)
    }

    // --- TEST ESTADO INICIAL ---
    @Test
    fun al_iniciar_el_formulario_login_esta_vacio() {
        val estado = viewModel.login.value
        assertEquals("", estado.email)
        assertEquals("", estado.pass)
        assertFalse(estado.canSubmit)
    }

    // --- TEST VALIDACIONES ---
    @Test
    fun al_escribir_email_invalido_aparece_error() {
        viewModel.onLoginEmailChange("correo-malo")
        
        val estado = viewModel.login.value
        assertEquals("Formato de email inválido", estado.emailError)
        assertFalse(estado.canSubmit)
    }

    @Test
    fun al_escribir_datos_validos_se_habilita_boton_login() {
        viewModel.onLoginEmailChange("juan@gmail.com")
        viewModel.onLoginPassChange("123456")

        val estado = viewModel.login.value
        assertNull(estado.emailError)
        assertTrue(estado.canSubmit)
    }

    // --- TEST LOGIN EXITOSO ---
    @Test
    fun submitLogin_con_credenciales_correctas_cambia_estado_a_exito() {
        // ARRANGE
        val usuarioFake = UserEntity(
            id = 1L, 
            name = "Juan", 
            email = "juan@gmail.com", 
            phone = "12345678", 
            pass = "Pass123!", 
            role = "USER"
        )
        
        coEvery { repository.login("juan@gmail.com", "Pass123!") } returns Result.success(usuarioFake)
        
        // Simulamos que guardar en preferencias no hace nada (y es rapido)
        coEvery { userPreferences.setLoggedIn(true) } returns Unit
        coEvery { userPreferences.saveUserRole("USER") } returns Unit

        // ACT
        viewModel.onLoginEmailChange("juan@gmail.com")
        viewModel.onLoginPassChange("Pass123!")
        viewModel.submitLogin()

        // Avanzamos el tiempo
        ShadowLooper.runUiThreadTasksIncludingDelayedTasks()

        // ASSERT
        val estado = viewModel.login.value
        assertTrue("El estado success debería ser true", estado.success)
        assertEquals("USER", estado.loggedInUserRole)
        assertNull(estado.errorMsg)
    }

    // --- TEST LOGIN FALLIDO ---
    @Test
    fun submitLogin_con_error_muestra_mensaje() {
        // ARRANGE
        coEvery { repository.login(any(), any()) } returns Result.failure(Exception("Error"))

        // ACT
        viewModel.onLoginEmailChange("juan@gmail.com")
        viewModel.onLoginPassChange("MalPass")
        viewModel.submitLogin()

        ShadowLooper.runUiThreadTasksIncludingDelayedTasks()

        // ASSERT
        val estado = viewModel.login.value
        assertFalse(estado.success)
        assertEquals("Credenciales inválidas", estado.errorMsg)
    }
}
