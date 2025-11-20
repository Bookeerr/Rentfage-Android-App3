package com.example.rentfage.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rentfage.data.repository.UserRepository
import com.example.rentfage.domain.validation.* // Importamos las validaciones
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PerfilUiState(
    val name: String = "Cargando...",
    val email: String = "",
    val phone: String = "",
    val initials: String = "--"
)

data class EditProfileUiState(
    val name: String = "",
    val phone: String = "",
    val nameError: String? = null,
    val phoneError: String? = null,
    val canSubmit: Boolean = false
)

class PerfilViewModel(private val userRepository: UserRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(PerfilUiState())
    val uiState: StateFlow<PerfilUiState> = _uiState.asStateFlow()

    private val _editProfileState = MutableStateFlow(EditProfileUiState())
    val editProfileState: StateFlow<EditProfileUiState> = _editProfileState

    init {
        cargarDatosUsuario()
    }

    fun cargarDatosUsuario() {
        val emailUsuarioActivo = AuthViewModel.activeUserEmail
        if (emailUsuarioActivo != null) {
            viewModelScope.launch {
                val usuarioEncontrado = userRepository.getUserByEmail(emailUsuarioActivo)
                if (usuarioEncontrado != null) {
                    _uiState.value = PerfilUiState(
                        name = usuarioEncontrado.name,
                        email = usuarioEncontrado.email,
                        phone = usuarioEncontrado.phone,
                        initials = getInitials(usuarioEncontrado.name)
                    )
                    // Inicializar el estado de edición con los datos actuales
                    _editProfileState.value = EditProfileUiState(
                        name = usuarioEncontrado.name,
                        phone = usuarioEncontrado.phone
                    )
                    // Revalidar para habilitar el botón si los datos ya son válidos
                    recomputeEditCanSubmit()
                } else {
                     _uiState.value = PerfilUiState(name = "Usuario no encontrado")
                }
            }
        } else {
            _uiState.value = PerfilUiState(name = "No hay sesión activa")
        }
    }

    fun onEditNameChange(value: String) {
        // Usamos la validación global del profesor
        _editProfileState.update {
            it.copy(name = value, nameError = validateNameLettersOnly(value))
        }
        recomputeEditCanSubmit()
    }

    fun onEditPhoneChange(value: String) {
        // Usamos la validación global del profesor
        _editProfileState.update {
            it.copy(phone = value, phoneError = validatePhoneDigitsOnly(value))
        }
        recomputeEditCanSubmit()
    }

    private fun recomputeEditCanSubmit() {
        val s = _editProfileState.value
        val noErrors = s.nameError == null && s.phoneError == null
        val filled = s.name.isNotBlank() && s.phone.isNotBlank()
        _editProfileState.update { it.copy(canSubmit = noErrors && filled) }
    }

    fun updateUser(onUpdateFinished: () -> Unit) {
        val editState = _editProfileState.value
        if (!editState.canSubmit) return

        val emailUsuarioActivo = AuthViewModel.activeUserEmail
        if (emailUsuarioActivo != null) {
            viewModelScope.launch {
                val result = userRepository.updateProfile(
                    email = emailUsuarioActivo,
                    newName = editState.name.trim(),
                    newPhone = editState.phone.trim()
                )

                if (result.isSuccess) {
                    cargarDatosUsuario()
                    onUpdateFinished()
                }
            }
        }
    }

    private fun getInitials(name: String): String {
        return name.split(' ')
            .filter { it.isNotBlank() }
            .take(2)
            .map { it.first().uppercase() }
            .joinToString("")
    }
}
