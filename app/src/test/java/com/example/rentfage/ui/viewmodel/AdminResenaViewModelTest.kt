package com.example.rentfage.ui.viewmodel

import com.example.rentfage.data.local.entity.ResenaEntidad
import com.example.rentfage.data.repository.ResenaRepositorio
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowLooper

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class AdminResenaViewModelTest {

    private lateinit var viewModel: AdminResenaViewModel
    private lateinit var repository: ResenaRepositorio

    @Before
    fun setUp() {
        repository = mockk(relaxed = true)
    }

    @Test
    fun al_iniciar_carga_todas_las_resenas_del_repositorio() {
        // 1. Arrange: Preparamos datos falsos
        // CORRECCIÓN: 'userId' debe ser Int (1), no Long (1L)
        val listaResenas = listOf(
            ResenaEntidad(id = 1, userId = 1, comentario = "Reseña 1", fechaCreacion = 1000L),
            ResenaEntidad(id = 2, userId = 2, comentario = "Reseña 2", fechaCreacion = 2000L)
        )
        
        // Simulamos que el repositorio emite esta lista inmediatamente
        every { repository.todasLasResenas } returns flowOf(listaResenas)

        viewModel = AdminResenaViewModel(repository)
        
        // 2. Act: "Despertamos" al StateFlow suscribiéndonos a él.
        val scope = CoroutineScope(Dispatchers.Unconfined)
        val job = scope.launch {
            viewModel.todasLasResenas.collect {}
        }

        // Dejamos que Robolectric procese los eventos pendientes
        ShadowLooper.runUiThreadTasksIncludingDelayedTasks()

        val listaActual = viewModel.todasLasResenas.value
        
        // Limpieza
        job.cancel()
        scope.cancel()

        // 3. Assert: Verificamos
        assertEquals(2, listaActual.size)
        assertEquals("Reseña 1", listaActual[0].comentario)
    }
}