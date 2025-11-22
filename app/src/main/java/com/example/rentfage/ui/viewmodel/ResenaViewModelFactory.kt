package com.example.rentfage.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.rentfage.data.repository.ResenaRepositorio

class ResenaViewModelFactory(private val repository: ResenaRepositorio) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ResenaViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ResenaViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}