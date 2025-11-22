package com.example.rentfage.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rentfage.data.local.entity.ResenaEntidad
import com.example.rentfage.data.repository.ResenaRepositorio
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// Estado de la UI para la pantalla de reseña
data class ResenaUiState(
    val comentario: String = "", // Siempre empieza vacío para la nueva reseña
    val reseñaExistente: ResenaEntidad? = null,
    val isLoading: Boolean = true,
    val saveSuccess: Boolean = false
)

class ResenaViewModel(private val resenaRepositorio: ResenaRepositorio) : ViewModel() {

    private val _uiState = MutableStateFlow(ResenaUiState())
    val uiState: StateFlow<ResenaUiState> = _uiState.asStateFlow()

    fun onComentarioChange(nuevoComentario: String) {
        //  límite de 300 caracteres
        if (nuevoComentario.length <= 300) {
            _uiState.update { it.copy(comentario = nuevoComentario) }
        }
    }

    // Carga la reseña existente, pero NO la pone en el campo de texto editable
    fun cargarResenaDeUsuario(userId: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val resena = resenaRepositorio.obtenerResenaDeUsuario(userId)
            _uiState.update {
                it.copy(
                    reseñaExistente = resena, // Solo guardamos la reseña para mostrarla
                    comentario = "", // Nos aseguramos de que el campo para escribir esté vacío
                    isLoading = false
                )
            }
        }
    }

    // Al enviar, limpiamos el campo y recargamos la reseña para mostrar la nueva
    fun enviarResena(userId: Int) {
        if (_uiState.value.comentario.isBlank()) return

        viewModelScope.launch {
            resenaRepositorio.enviarResena(userId, _uiState.value.comentario.trim())
            _uiState.update { it.copy(saveSuccess = true, comentario = "") } // Limpiamos el comentario
            cargarResenaDeUsuario(userId) // Recargamos para mostrar la nueva reseña abajo
        }
    }
    
    fun resetSaveStatus() {
        _uiState.update { it.copy(saveSuccess = false) }
    }
}