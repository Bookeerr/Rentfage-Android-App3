package com.example.rentfage.ui.viewmodel

import com.example.rentfage.data.local.entity.CasaEntity
import com.example.rentfage.data.repository.CasasRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowLooper

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class CasasViewModelTest {

    private lateinit var viewModel: CasasViewModel
    private lateinit var repository: CasasRepository

    @Before
    fun setUp() {
        repository = mockk(relaxed = true)
        // Configuramos respuestas por defecto para evitar errores al iniciar el ViewModel
        coEvery { repository.todasLasCasas } returns flowOf(emptyList())
        coEvery { repository.casasFavoritas } returns flowOf(emptyList())
        
        viewModel = CasasViewModel(repository)
    }

    @Test
    fun al_iniciar_carga_la_lista_de_casas() {
        // Arrange
        val listaCasas = listOf(
            CasaEntity(id = 1, price = "100", address = "Calle 1", details = "D", imageUri = "U", latitude = 0.0, longitude = 0.0)
        )
        val repoTest = mockk<CasasRepository>(relaxed = true)
        coEvery { repoTest.todasLasCasas } returns flowOf(listaCasas)
        coEvery { repoTest.casasFavoritas } returns flowOf(emptyList())
        
        // Act
        val vm = CasasViewModel(repoTest)
        // Ya no se necesitan trucos. El StateFlow se inicia Eagerly.
        ShadowLooper.runUiThreadTasksIncludingDelayedTasks()

        // Assert
        assertEquals(1, vm.uiState.value.casas.size)
        assertEquals("Calle 1", vm.uiState.value.casas[0].address)
    }

    @Test
    fun toggleFavorite_llama_al_repositorio() {
        val casa = CasaEntity(id = 1, price = "100", address = "Calle 1", details = "D", imageUri = "U", latitude = 0.0, longitude = 0.0, isFavorite = false)
        
        viewModel.toggleFavorite(casa)
        ShadowLooper.runUiThreadTasksIncludingDelayedTasks()

        // Verifica que se guardó con isFavorite = true
        coVerify { repository.actualizarCasa(match { it.id == 1 && it.isFavorite == true }) }
    }

    @Test
    fun deleteCasa_llama_al_repositorio() {
        val casa = CasaEntity(id = 1, price = "100", address = "Calle 1", details = "D", imageUri = "U", latitude = 0.0, longitude = 0.0)
        
        viewModel.deleteCasa(casa)
        ShadowLooper.runUiThreadTasksIncludingDelayedTasks()

        coVerify { repository.borrarCasa(casa) }
    }

    @Test
    fun saveProperty_guarda_si_formulario_valido() {
        // Arrange: Llenamos el formulario
        viewModel.onAddressChange("Av Siempre Viva")
        viewModel.onPriceChange("100000")
        viewModel.onDetailsChange("Detalles")
        viewModel.onLatitudeChange("10.0")
        viewModel.onLongitudeChange("20.0")
        viewModel.onImageUriChange("uri")

        // Act: Guardar nueva casa (id = null)
        viewModel.saveProperty(null)
        ShadowLooper.runUiThreadTasksIncludingDelayedTasks()

        // Assert
        coVerify { repository.insertarCasa(any()) }
        assertTrue(viewModel.addEditState.value.saveSuccess)
    }

    @Test
    fun saveProperty_no_guarda_si_faltan_datos() {
        // Arrange: Formulario incompleto (solo direccion)
        viewModel.resetAddEditState()
        viewModel.onAddressChange("Solo tengo direccion")

        // Act
        viewModel.saveProperty(null)
        ShadowLooper.runUiThreadTasksIncludingDelayedTasks()

        // Assert: NO debe llamar a insertar
        coVerify(exactly = 0) { repository.insertarCasa(any()) }
    }
}
