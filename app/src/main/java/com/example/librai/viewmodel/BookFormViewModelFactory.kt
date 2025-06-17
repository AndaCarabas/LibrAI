package com.example.librai.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.librai.data.repository.BookRepository

class BookFormViewModelFactory (private val repository: BookRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BookFormViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return BookFormViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}