package com.example.rentfage.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.rentfage.data.repository.ResenaRepositorio

class AdminResenaViewModelFactory(private val repository: ResenaRepositorio) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AdminResenaViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AdminResenaViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}