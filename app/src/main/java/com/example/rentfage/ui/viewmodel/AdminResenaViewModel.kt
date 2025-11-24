package com.example.rentfage.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rentfage.data.local.entity.ResenaEntidad
import com.example.rentfage.data.repository.ResenaRepositorio
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AdminResenaViewModel(private val resenaRepositorio: ResenaRepositorio) : ViewModel() {

    val todasLasResenas: StateFlow<List<ResenaEntidad>> = resenaRepositorio.todasLasResenas
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // --- SINCRONIZACIÓN AUTOMÁTICA ---
    init {
        sincronizar()
    }

    private fun sincronizar() {
        viewModelScope.launch {
            // Llama al repositorio para traer las reseñas desde el microservicio.
            // Si falla, el runCatching en el repositorio se encargará y la UI
            // simplemente mostrará la lista local (que estaría vacía).
            resenaRepositorio.sincronizarResenas()
        }
    }
}