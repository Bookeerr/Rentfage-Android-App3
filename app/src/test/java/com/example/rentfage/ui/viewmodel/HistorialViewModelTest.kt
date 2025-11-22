package com.example.rentfage.ui.viewmodel

import com.example.rentfage.data.local.entity.CasaEntity
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowLooper

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class HistorialViewModelTest {

    private lateinit var viewModel: HistorialViewModel

    @Before
    fun setUp() {
        // Creamos una nueva instancia limpia para cada test
        viewModel = HistorialViewModel()
        // Limpiamos el usuario activo antes de cada test
        AuthViewModel.activeUserEmail = null
    }

    @Test
    fun addSolicitud_cuandoHayUsuario_anadeLaSolicitud() {
        // Arrange
        AuthViewModel.activeUserEmail = "user1@test.com"
        val casa = CasaEntity(id = 1, price = "100", address = "A", details = "D", imageUri = "U", latitude = 0.0, longitude = 0.0)

        // Act
        viewModel.addSolicitud(casa)
        ShadowLooper.runUiThreadTasksIncludingDelayedTasks()

        // Assert
        val solicitudes = viewModel.uiState.value.solicitudes
        assertEquals(1, solicitudes.size)
        assertEquals("user1@test.com", solicitudes[0].usuarioEmail)
    }

    @Test
    fun cargarSolicitudesDeUsuario_soloMuestraLasDelUsuarioActivo() {
        // Arrange: Añadimos dos solicitudes con usuarios diferentes
        AuthViewModel.activeUserEmail = "user1@test.com"
        viewModel.addSolicitud(CasaEntity(id = 1, price = "", address = "", details = "", imageUri = "", latitude = 0.0, longitude = 0.0))
        
        AuthViewModel.activeUserEmail = "user2@test.com"
        viewModel.addSolicitud(CasaEntity(id = 2, price = "", address = "", details = "", imageUri = "", latitude = 0.0, longitude = 0.0))

        // Act: Nos logueamos como el primer usuario y recargamos
        AuthViewModel.activeUserEmail = "user1@test.com"
        viewModel.cargarSolicitudesDeUsuario()
        ShadowLooper.runUiThreadTasksIncludingDelayedTasks()

        // Assert
        val solicitudes = viewModel.uiState.value.solicitudes
        assertEquals(1, solicitudes.size)
        assertEquals("user1@test.com", solicitudes[0].usuarioEmail)
    }

    @Test
    fun aprobarSolicitud_cambiaElEstadoCorrectamente() {
        // Arrange
        AuthViewModel.activeUserEmail = "admin@test.com"
        viewModel.addSolicitud(CasaEntity(id = 1, price = "", address = "", details = "", imageUri = "", latitude = 0.0, longitude = 0.0))
        
        // Para obtener el id, primero cargamos todas las solicitudes
        viewModel.cargarTodasLasSolicitudes()
        val solicitudId = viewModel.uiState.value.solicitudes[0].id

        // Act
        viewModel.aprobarSolicitud(solicitudId)
        ShadowLooper.runUiThreadTasksIncludingDelayedTasks()

        // Assert
        val solicitudActualizada = viewModel.uiState.value.solicitudes.find { it.id == solicitudId }
        assertEquals(EstadoSolicitud.Aprobada, solicitudActualizada?.estado)
    }
}
