package com.example.rentfage.ui.viewmodel

import com.example.rentfage.data.local.entity.ResenaEntidad
import com.example.rentfage.data.repository.ResenaRepositorio
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
class ResenaViewModelTest {

    private lateinit var viewModel: ResenaViewModel
    private lateinit var repository: ResenaRepositorio

    @Before
    fun setUp() {
        repository = mockk(relaxed = true)
        viewModel = ResenaViewModel(repository)
    }

    @Test
    fun `onComentarioChange_respeta_el_limite_de_300_caracteres`() {
        val textoLargo = "a".repeat(301)
        
        // Act: Intentamos poner un texto de 301 caracteres
        viewModel.onComentarioChange(textoLargo)
        
        // Assert: El comentario en el estado no debe tener más de 300
        // (Como la lógica es 'if(length <= 300)', el estado no se actualiza)
        assertEquals(0, viewModel.uiState.value.comentario.length)

        // Act 2: Probamos con un texto válido
        val textoValido = "a".repeat(300)
        viewModel.onComentarioChange(textoValido)
        assertEquals(300, viewModel.uiState.value.comentario.length)
    }

    @Test
    fun `cargarResenaDeUsuario_muestra_la_existente_y_limpia_el_campo_nuevo`() {
        // Arrange: Simulamos que el repositorio devuelve una reseña
        val userId = 1
        val reseñaAnterior = ResenaEntidad(id = 1, userId = userId, comentario = "Mi vieja opinión")
        coEvery { repository.obtenerResenaDeUsuario(userId) } returns reseñaAnterior

        // Act: Cargamos la reseña
        viewModel.cargarResenaDeUsuario(userId)
        ShadowLooper.runUiThreadTasksIncludingDelayedTasks()

        // Assert
        val state = viewModel.uiState.value
        assertEquals(reseñaAnterior, state.reseñaExistente) // La reseña guardada está en su sitio
        assertEquals("", state.comentario) // El campo para escribir está vacío
    }

    @Test
    fun `enviarResena_llama_al_repositorio_y_limpia_el_comentario`() {
        // Arrange: Escribimos en el campo de texto
        val userId = 1
        val nuevoComentario = "Esta es una nueva reseña"
        viewModel.onComentarioChange(nuevoComentario)

        // Act: Enviamos la reseña
        viewModel.enviarResena(userId)
        ShadowLooper.runUiThreadTasksIncludingDelayedTasks()

        // Assert
        // 1. Verificamos que se llamó a guardar en el repositorio
        coVerify { repository.enviarResena(userId, nuevoComentario) }

        // 2. Verificamos que el campo de texto se limpió
        assertEquals("", viewModel.uiState.value.comentario)
    }
}