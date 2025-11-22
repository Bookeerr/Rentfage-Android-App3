package com.example.rentfage.ui.viewmodel

import com.example.rentfage.data.local.entity.UserEntity
import com.example.rentfage.data.repository.UserRepository
import io.mockk.coEvery
import io.mockk.coVerify
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
class PerfilViewModelTest {

    private lateinit var viewModel: PerfilViewModel
    private lateinit var repository: UserRepository

    @Before
    fun setUp() {
        repository = mockk(relaxed = true)
        viewModel = PerfilViewModel(repository)
    }

    @Test
    fun cargarDatosUsuario_debeActualizarElEstado_conDatosDelUsuario() {
        // Arrange: Simulamos un usuario logueado
        AuthViewModel.activeUserEmail = "test@test.com"
        val usuarioFake = UserEntity(id = 1L, name = "Juan Tester", email = "test@test.com", phone = "12345678", pass = "", role = "USER")
        coEvery { repository.getUserByEmail("test@test.com") } returns usuarioFake

        // Act: Cargamos los datos
        viewModel.cargarDatosUsuario()
        ShadowLooper.runUiThreadTasksIncludingDelayedTasks()

        // Assert: Verificamos el estado
        val estado = viewModel.uiState.value
        assertEquals("Juan Tester", estado.name)
        assertEquals("test@test.com", estado.email)
        assertEquals("JT", estado.initials)
    }

    @Test
    fun alEditarNombre_conDatoInvalido_seMuestraErrorDeValidacion() {
        // Act: El usuario escribe un nombre con números
        viewModel.onEditNameChange("Nombre 123")

        // Assert: El estado debe reflejar el error
        val estado = viewModel.editProfileState.value
        assertEquals("Solo letras y espacios", estado.nameError)
        assertFalse(estado.canSubmit)
    }

    @Test
    fun updateUser_cuandoFormularioEsValido_llamaAlRepositorioCorrectamente() {
        // Arrange: Simulamos el usuario logueado y llenamos el formulario
        AuthViewModel.activeUserEmail = "test@test.com"
        viewModel.onEditNameChange("Nombre Nuevo")
        viewModel.onEditPhoneChange("87654321")

        // Act: El usuario guarda los cambios
        viewModel.updateUser { /* lambda vacía */ }
        ShadowLooper.runUiThreadTasksIncludingDelayedTasks()

        // Assert: Verificamos que se llamó a la función de actualizar
        coVerify { repository.updateProfile("test@test.com", "Nombre Nuevo", "87654321") }
    }
}
