package com.example.rentfage.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rentfage.data.local.entity.ResenaEntidad
import com.example.rentfage.data.repository.ResenaRepositorio
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class AdminResenaUiState(
    val resenas: List<ResenaEntidad> = emptyList()
)

class AdminResenaViewModel(private val repository: ResenaRepositorio) : ViewModel() {

    val uiState: StateFlow<AdminResenaUiState> = repository.todasLasResenas
        .map { resenas -> AdminResenaUiState(resenas = resenas) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly, // CAMBIADO A Eagerly
            initialValue = AdminResenaUiState()
        )
}
