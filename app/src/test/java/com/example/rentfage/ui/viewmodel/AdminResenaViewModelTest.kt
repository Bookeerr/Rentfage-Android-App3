package com.example.rentfage.ui.viewmodel

import com.example.rentfage.data.local.entity.ResenaEntidad
import com.example.rentfage.data.repository.ResenaRepositorio
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowLooper
import kotlinx.coroutines.flow.flowOf

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class AdminResenaViewModelTest {

    private lateinit var viewModel: AdminResenaViewModel
    private lateinit var repository: ResenaRepositorio

    @Before
    fun setUp() {
        // Solo preparamos el mock. El ViewModel se crea en cada test.
        repository = mockk(relaxed = true)
    }

    @Test
    fun al_iniciar_carga_todas_las_resenas_del_repositorio() {
        // Arrange: Preparamos datos falsos
        val listaResenas = listOf(
            ResenaEntidad(id = 1, userId = 1, comentario = "Reseña 1"),
            ResenaEntidad(id = 2, userId = 2, comentario = "Reseña 2")
        )
        // Simulamos que el repositorio emite esta lista
        every { repository.todasLasResenas } returns flowOf(listaResenas)

        // Act: Creamos el ViewModel
        viewModel = AdminResenaViewModel(repository)
        
        // IMPORTANTE: Dejamos que Robolectric avance los hilos para que el StateFlow se actualice
        ShadowLooper.runUiThreadTasksIncludingDelayedTasks()

        // Assert: Ahora sí, el valor del state debería estar actualizado
        val estado = viewModel.uiState.value
        assertEquals(2, estado.resenas.size)
        assertEquals("Reseña 1", estado.resenas[0].comentario)
    }
}