package com.example.rentfage.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.rentfage.data.repository.CasasRepository
import com.example.rentfage.data.repository.ComprasRepository
import com.example.rentfage.data.repository.UserRepository

class HistorialViewModelFactory(
    private val comprasRepository: ComprasRepository,
    private val casasRepository: CasasRepository,
    private val userRepository: UserRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HistorialViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HistorialViewModel(comprasRepository, casasRepository, userRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}