package com.example.rentfage.ui.viewmodel

import com.example.rentfage.data.local.entity.CasaEntity
import com.example.rentfage.data.local.entity.UserEntity
import com.example.rentfage.data.repository.CasasRepository
import com.example.rentfage.data.repository.ComprasRepository
import com.example.rentfage.data.repository.UserRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
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
    private lateinit var comprasRepository: ComprasRepository
    private lateinit var casasRepository: CasasRepository
    private lateinit var userRepository: UserRepository

    @Before
    fun setUp() {
        // 1. Creamos Mocks (simulaciones) de los repositorios
        comprasRepository = mockk(relaxed = true)
        casasRepository = mockk(relaxed = true)
        userRepository = mockk(relaxed = true)

        // 2. Simulamos respuestas básicas para que el ViewModel no falle al iniciar
        // (El ViewModel intenta cargar listas en el init)
        every { comprasRepository.historialDeUsuario(any()) } returns flowOf(emptyList())
        every { comprasRepository.todasLasSolicitudes } returns flowOf(emptyList())
        every { casasRepository.todasLasCasas } returns flowOf(emptyList())
        
        // Simulamos un usuario activo por defecto
        AuthViewModel.activeUserEmail = "test@user.com"
    }

    @Test
    fun addSolicitud_llamaAlRepositorioCorrectamente() {
        // Arrange: Preparamos el escenario
        val emailUsuario = "test@user.com"
        AuthViewModel.activeUserEmail = emailUsuario
        
        val casa = CasaEntity(id = 1, price = "100", address = "Calle Falsa 123", details = "Detalle", imageUri = "", latitude = 0.0, longitude = 0.0)
        val usuarioMock = UserEntity(id = 99, name = "Test", email = emailUsuario, phone = "123", pass = "123", role = "USER")

        // Simulamos que el repositorio de usuarios encuentra al usuario
        coEvery { userRepository.getUserByEmail(emailUsuario) } returns usuarioMock
        
        // Simulamos que enviar la solicitud funciona
        coEvery { comprasRepository.enviarSolicitud(any(), any(), any()) } returns Result.success(Unit)

        // Inicializamos el ViewModel (esto dispara el init y las llamadas iniciales)
        viewModel = HistorialViewModel(comprasRepository, casasRepository, userRepository)
        ShadowLooper.runUiThreadTasksIncludingDelayedTasks()

        // Act: Ejecutamos la acción de añadir solicitud
        viewModel.addSolicitud(casa)
        ShadowLooper.runUiThreadTasksIncludingDelayedTasks()

        // Assert: Verificamos que el ViewModel llamó a la función 'enviarSolicitud' del repositorio
        // con los parámetros correctos (ID de usuario 99 y ID de casa 1)
        coVerify { 
            comprasRepository.enviarSolicitud(
                userId = 99, 
                casaId = 1, 
                userEmail = emailUsuario
            ) 
        }
    }

    @Test
    fun aprobarSolicitud_llamaAlRepositorioParaActualizarEstado() {
        // Arrange
        viewModel = HistorialViewModel(comprasRepository, casasRepository, userRepository)
        val solicitudId = 123

        // Act
        viewModel.aprobarSolicitud(solicitudId)
        ShadowLooper.runUiThreadTasksIncludingDelayedTasks()

        // Assert
        coVerify { 
            comprasRepository.actualizarEstado(123, "Aprobada")
        }
    }
}