package com.example.rentfage.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rentfage.data.local.entity.CasaEntity
import com.example.rentfage.data.local.entity.SolicitudEntity
import com.example.rentfage.data.repository.CasasRepository
import com.example.rentfage.data.repository.ComprasRepository
import com.example.rentfage.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class EstadoSolicitud { Pendiente, Aprobada, Rechazada }

data class SolicitudUi(
    val id: Int,
    val usuarioEmail: String,
    val casa: CasaEntity?,
    val nombreCasa: String,
    val fecha: String,
    val estado: EstadoSolicitud
)

data class HistorialUiState(
    val solicitudes: List<SolicitudUi> = emptyList(),
    val isLoading: Boolean = false
)

class HistorialViewModel(
    private val comprasRepository: ComprasRepository,
    private val casasRepository: CasasRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HistorialUiState(isLoading = true))
    val uiState: StateFlow<HistorialUiState> = _uiState.asStateFlow()

    private val _messageFlow = MutableSharedFlow<String>()
    val messageFlow: SharedFlow<String> = _messageFlow.asSharedFlow()

    init {
        observarSolicitudes()
    }

    private fun observarSolicitudes() {
        val currentUserEmail = AuthViewModel.activeUserEmail
        val isAdmin = currentUserEmail == "admin@rent.cl"

        val flujoSolicitudes = when {
            isAdmin -> comprasRepository.todasLasSolicitudes
            currentUserEmail != null -> comprasRepository.historialDeUsuario(currentUserEmail)
            else -> null
        }

        if (flujoSolicitudes == null) {
            _uiState.update { it.copy(solicitudes = emptyList(), isLoading = false) }
            return
        }

        combine(flujoSolicitudes, casasRepository.todasLasCasas) { solicitudes, casas ->
            solicitudes.map { solicitud ->
                var casaDetalle = casas.find { it.id == solicitud.casaId }

                val nombrePropiedad = solicitud.tituloPropiedad
                    ?: casaDetalle?.details?.lines()?.firstOrNull()
                    ?: casaDetalle?.address
                    ?: "Propiedad #${solicitud.casaId}"

                if (casaDetalle == null) {
                    casaDetalle = CasaEntity(
                        id = solicitud.casaId,
                        price = "Consultar",
                        address = solicitud.tituloPropiedad ?: "Casa #${solicitud.casaId}",
                        details = "Detalles no disponibles offline",
                        imageUri = "",
                        latitude = 0.0,
                        longitude = 0.0
                    )
                }

                SolicitudUi(
                    id = solicitud.id,
                    usuarioEmail = solicitud.usuarioEmail,
                    casa = casaDetalle,
                    nombreCasa = nombrePropiedad,
                    fecha = solicitud.fecha,
                    estado = runCatching { EstadoSolicitud.valueOf(solicitud.estado) }
                        .getOrElse { EstadoSolicitud.Pendiente }
                )
            }
        }
            .onEach { lista ->
                _uiState.update { it.copy(solicitudes = lista, isLoading = false) }
            }
            .launchIn(viewModelScope)

        viewModelScope.launch {
            if (isAdmin) {
                comprasRepository.sincronizarTodas().getOrElse {
                    // No hacemos nada si falla, para no molestar al usuario
                }
            } else if (currentUserEmail != null) {
                val usuario = userRepository.getUserByEmail(currentUserEmail)
                val userId = usuario?.id ?: 0L
                comprasRepository.sincronizarSolicitudes(currentUserEmail, userId).getOrElse {
                    // No hacemos nada si falla (ej. 404), el usuario ve los datos locales
                }
            }
        }
    }

    fun addSolicitud(casa: CasaEntity) {
        val currentUserEmail = AuthViewModel.activeUserEmail ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val user = userRepository.getUserByEmail(currentUserEmail)
            val userId = user?.id ?: 0L
            comprasRepository.enviarSolicitud(
                userId = userId,
                casaId = casa.id.toLong(),
                userEmail = currentUserEmail
            ).onSuccess {
                _messageFlow.emit("Solicitud enviada con éxito")
            }.onFailure {
                _messageFlow.emit("Error al enviar solicitud: ${it.message}")
            }
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    fun aprobarSolicitud(solicitudId: Int) {
        actualizarEstado(solicitudId, "Aprobada")
    }

    fun rechazarSolicitud(solicitudId: Int) {
        actualizarEstado(solicitudId, "Rechazada")
    }

    private fun actualizarEstado(id: Int, nuevoEstado: String) {
        viewModelScope.launch {
            comprasRepository.actualizarEstado(id.toLong(), nuevoEstado)
                .onSuccess { _messageFlow.emit("Solicitud $nuevoEstado") }
                .onFailure { _messageFlow.emit("Error al actualizar: ${it.message}") }
        }
    }
}