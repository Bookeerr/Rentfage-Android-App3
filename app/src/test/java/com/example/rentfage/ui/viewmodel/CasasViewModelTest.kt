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
    fun setup() {
        // 1. Creamos el Mock del repositorio
        repository = mockk(relaxed = true)
        
        // Configuramos respuestas por defecto para evitar errores al iniciar el ViewModel
        coEvery { repository.todasLasCasas } returns flowOf(emptyList())
        coEvery { repository.casasFavoritas } returns flowOf(emptyList())
        
        // 2. Iniciamos el ViewModel
        viewModel = CasasViewModel(repository)
    }

    // --- TEST DE CARGA INICIAL ---
    @Test
    fun al_iniciar_carga_la_lista_de_casas_desde_el_repositorio() {
        // Arrange: Preparamos una lista falsa
        val casasFalsas = listOf(
            CasaEntity(id = 1, price = "100", address = "Calle 1", details = "D", imageUri = "U", latitude = 0.0, longitude = 0.0)
        )
        // Creamos un repositorio especifico para este test
        val repoTest = mockk<CasasRepository>(relaxed = true)
        coEvery { repoTest.todasLasCasas } returns flowOf(casasFalsas)
        coEvery { repoTest.casasFavoritas } returns flowOf(emptyList())
        
        // Act: Iniciamos el ViewModel con ese repo
        val vm = CasasViewModel(repoTest)
        // Damos tiempo para que cargue (StateFlow)
        ShadowLooper.runUiThreadTasksIncludingDelayedTasks()

        // Assert
        assertEquals(1, vm.uiState.value.casas.size)
        assertEquals("Calle 1", vm.uiState.value.casas[0].address)
    }

    // --- TEST DE FAVORITOS ---
    @Test
    fun toggleFavorite_llama_al_repositorio_para_actualizar_estado() {
        // Arrange: Casa sin favorito
        val casa = CasaEntity(id = 1, price = "100", address = "Calle 1", details = "D", imageUri = "U", latitude = 0.0, longitude = 0.0, isFavorite = false)
        
        // Act: Pulsamos el corazon
        viewModel.toggleFavorite(casa)
        ShadowLooper.runUiThreadTasksIncludingDelayedTasks()

        // Assert: Verificamos que se guardo con favorite = true
        coVerify { repository.actualizarCasa(match { it.id == 1 && it.isFavorite == true }) }
    }

    // --- TEST DE BORRAR ---
    @Test
    fun deleteCasa_llama_al_repositorio_para_borrar() {
        val casa = CasaEntity(id = 1, price = "100", address = "Calle 1", details = "D", imageUri = "U", latitude = 0.0, longitude = 0.0)
        
        viewModel.deleteCasa(casa)
        ShadowLooper.runUiThreadTasksIncludingDelayedTasks()

        coVerify { repository.borrarCasa(casa) }
    }

    // --- TEST DE CREAR/EDITAR (ADMIN) ---
    @Test
    fun saveProperty_no_guarda_si_el_formulario_esta_incompleto() {
        // Arrange: Reseteamos formulario (vacio)
        viewModel.resetAddEditState()
        
        // Act: Intentamos guardar
        viewModel.saveProperty(null)
        ShadowLooper.runUiThreadTasksIncludingDelayedTasks()

        // Assert: No debio llamar a insertar
        coVerify(exactly = 0) { repository.insertarCasa(any()) }
    }

    @Test
    fun saveProperty_guarda_si_el_formulario_es_valido() {
        // Arrange: Llenamos todos los campos
        viewModel.onAddressChange("Av Siempre Viva")
        viewModel.onPriceChange("100000")
        viewModel.onDetailsChange("Linda casa")
        viewModel.onLatitudeChange("10.5")
        viewModel.onLongitudeChange("20.5")
        viewModel.onImageUriChange("uri_imagen")

        // Act: Guardamos (id = null significa nueva casa)
        viewModel.saveProperty(null)
        ShadowLooper.runUiThreadTasksIncludingDelayedTasks()

        // Assert: Verifica que se llamo a insertar y el estado cambio a exito
        coVerify { repository.insertarCasa(any()) }
        assertTrue(viewModel.addEditState.value.saveSuccess)
    }
}
