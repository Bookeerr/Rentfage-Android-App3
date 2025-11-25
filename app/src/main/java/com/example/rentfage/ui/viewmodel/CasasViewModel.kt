package com.example.rentfage.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rentfage.data.local.entity.CasaEntity
import com.example.rentfage.data.repository.CasasRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// --- ESTADO PARA LA LISTA DE CASAS ---
data class CasasUiState(
    val casas: List<CasaEntity> = emptyList(),
    val casasFavoritas: List<CasaEntity> = emptyList()
)

// --- ESTADO PARA EL FORMULARIO DE AÑADIR/EDITAR ---
data class AddEditCasaState(
    val address: String = "",
    val price: String = "",
    val details: String = "",
    val latitude: String = "",
    val longitude: String = "",
    val imageUri: String? = null,
    val canSubmit: Boolean = false,
    val isSaving: Boolean = false,
    val saveSuccess: Boolean = false
)

class CasasViewModel(private val casasRepository: CasasRepository) : ViewModel() {

    // --- LÓGICA PARA LA LISTA DE CASAS (EXISTENTE) ---
    val uiState: StateFlow<CasasUiState> = casasRepository.todasLasCasas.map { casas ->
        CasasUiState(casas = casas)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = CasasUiState()
    )

    val casasFavoritas: StateFlow<List<CasaEntity>> = casasRepository.casasFavoritas
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    init {
        sincronizar()
    }

    private fun sincronizar() {
        viewModelScope.launch {
            casasRepository.sincronizarCasas()
        }
    }

    fun toggleFavorite(casa: CasaEntity) {
        viewModelScope.launch {
            val updatedCasa = casa.copy(isFavorite = !casa.isFavorite)
            casasRepository.actualizarCasa(updatedCasa)
        }
    }

    fun getCasaById(id: Int): StateFlow<CasaEntity?> {
        return casasRepository.getById(id)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = null
            )
    }

    // --- LÓGICA PARA AÑADIR/EDITAR ---

    private val _addEditState = MutableStateFlow(AddEditCasaState())
    val addEditState: StateFlow<AddEditCasaState> = _addEditState.asStateFlow()

    fun loadCasaForEditingById(casaId: Int) {
        viewModelScope.launch {
            val casa = casasRepository.getById(casaId).firstOrNull()
            if (casa != null) {
                _addEditState.update {
                    it.copy(
                        address = casa.address,
                        price = casa.price.filter { char -> char.isDigit() },
                        details = casa.details,
                        latitude = casa.latitude.toString(),
                        longitude = casa.longitude.toString(),
                        imageUri = casa.imageUri,
                        canSubmit = true
                    )
                }
            }
        }
    }

    fun onAddressChange(newAddress: String) {
        _addEditState.update { it.copy(address = newAddress, canSubmit = checkCanSubmit(it.copy(address = newAddress))) }
    }
    fun onPriceChange(newPrice: String) {
        _addEditState.update { it.copy(price = newPrice, canSubmit = checkCanSubmit(it.copy(price = newPrice))) }
    }
    fun onDetailsChange(newDetails: String) {
        _addEditState.update { it.copy(details = newDetails, canSubmit = checkCanSubmit(it.copy(details = newDetails))) }
    }
    fun onLatitudeChange(newLat: String) {
        _addEditState.update { it.copy(latitude = newLat, canSubmit = checkCanSubmit(it.copy(latitude = newLat))) }
    }
    fun onLongitudeChange(newLon: String) {
        _addEditState.update { it.copy(longitude = newLon, canSubmit = checkCanSubmit(it.copy(longitude = newLon))) }
    }
    fun onImageUriChange(newUri: String?) {
        _addEditState.update { it.copy(imageUri = newUri, canSubmit = checkCanSubmit(it.copy(imageUri = newUri))) }
    }

    fun saveProperty(id: Int?) {
        // CORRECCIÓN: Validar antes de guardar
        if (!canSubmit()) return

        viewModelScope.launch {
            _addEditState.update { it.copy(isSaving = true) }

            val state = _addEditState.value
            val cleanedPrice = state.price.filter { it.isDigit() }

            val casa = CasaEntity(
                id = id ?: 0,
                address = state.address,
                price = "S/ $cleanedPrice",
                details = state.details,
                latitude = state.latitude.toDoubleOrNull() ?: 0.0,
                longitude = state.longitude.toDoubleOrNull() ?: 0.0,
                imageUri = state.imageUri ?: ""
            )

            if (id == null) {
                casasRepository.insertarCasa(casa)
            } else {
                casasRepository.actualizarCasa(casa)
            }

            sincronizar()
            _addEditState.update { it.copy(isSaving = false, saveSuccess = true) }
        }
    }

    fun resetAddEditState() {
        _addEditState.value = AddEditCasaState()
    }
    
    // Helper privado que accede al estado actual
    private fun canSubmit(): Boolean {
        return checkCanSubmit(_addEditState.value)
    }

    // Helper puro para verificar un estado dado (usado al actualizar campos)
    private fun checkCanSubmit(state: AddEditCasaState): Boolean {
        return state.address.isNotBlank() && 
               state.price.isNotBlank() && 
               state.details.isNotBlank() && 
               state.latitude.isNotBlank() && 
               state.longitude.isNotBlank()
    }

    fun deleteCasa(casa: CasaEntity) {
        viewModelScope.launch {
            casasRepository.borrarCasa(casa)
            sincronizar()
        }
    }
}