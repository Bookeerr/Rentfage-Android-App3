package com.example.rentfage.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rentfage.data.local.entity.CasaEntity
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class EstadoSolicitud { Pendiente, Aprobada, Rechazada }

data class Solicitud(
    val id: Int,
    val usuarioEmail: String,
    val casa: CasaEntity,
    val fecha: String,
    var estado: EstadoSolicitud
)

data class HistorialUiState(
    val solicitudes: List<Solicitud> = emptyList()
)

class HistorialViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(HistorialUiState())
    val uiState: StateFlow<HistorialUiState> = _uiState.asStateFlow()

    private val _messageFlow = MutableSharedFlow<String>()
    val messageFlow: SharedFlow<String> = _messageFlow.asSharedFlow()

    // Ya no es un companion object. Cada ViewModel tiene su propia lista.
    private val solicitudesEnMemoria = mutableListOf<Solicitud>()

    fun addSolicitud(casa: CasaEntity) {
        val currentUserEmail = AuthViewModel.activeUserEmail ?: return
        
        val newId = (solicitudesEnMemoria.maxOfOrNull { it.id } ?: 0) + 1
        val fechaActual = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())

        val nuevaSolicitud = Solicitud(
            id = newId,
            usuarioEmail = currentUserEmail,
            casa = casa,
            fecha = fechaActual,
            estado = EstadoSolicitud.Pendiente
        )

        solicitudesEnMemoria.add(nuevaSolicitud)
        cargarSolicitudesDeUsuario() // Actualiza la UI para el usuario actual
    }

    fun cargarSolicitudesDeUsuario() {
        val currentUserEmail = AuthViewModel.activeUserEmail
        _uiState.update {
            val solicitudesFiltradas = if (currentUserEmail != null) {
                solicitudesEnMemoria.filter { s -> s.usuarioEmail == currentUserEmail }
            } else {
                emptyList()
            }
            it.copy(solicitudes = solicitudesFiltradas)
        }
    }

    // --- Funciones de Admin ---

    fun cargarTodasLasSolicitudes() {
        _uiState.update { it.copy(solicitudes = solicitudesEnMemoria) }
    }

    fun aprobarSolicitud(solicitudId: Int) {
        solicitudesEnMemoria.find { it.id == solicitudId }?.let {
            it.estado = EstadoSolicitud.Aprobada
            cargarTodasLasSolicitudes() // Recargamos para que el admin vea el cambio
            viewModelScope.launch {
                _messageFlow.emit("Solicitud #${it.id} aprobada con éxito")
            }
        }
    }

    fun rechazarSolicitud(solicitudId: Int) {
        solicitudesEnMemoria.find { it.id == solicitudId }?.let {
            it.estado = EstadoSolicitud.Rechazada
            cargarTodasLasSolicitudes()
            viewModelScope.launch {
                _messageFlow.emit("Solicitud #${it.id} rechazada")
            }
        }
    }
}
