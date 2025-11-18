package com.example.rentfage.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rentfage.data.local.entity.UserEntity
import com.example.rentfage.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class UserViewModel(private val userRepository: UserRepository) : ViewModel() {

    private val _users = MutableStateFlow<List<UserEntity>>(emptyList())
    val users: StateFlow<List<UserEntity>> = _users.asStateFlow()

    init {
        loadUsers()
    }

    private fun loadUsers() {
        viewModelScope.launch {
            _users.value = userRepository.getAllUsers()
        }
    }

    fun updateUserRole(user: UserEntity, isAdmin: Boolean) {
        viewModelScope.launch {
            if (isAdmin) {
                userRepository.promoteToAdmin(user)
            } else {
                userRepository.demoteToUser(user)
            }
            loadUsers()
        }
    }
}
