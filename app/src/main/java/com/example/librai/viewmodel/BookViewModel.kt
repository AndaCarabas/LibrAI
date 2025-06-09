package com.example.librai.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.librai.models.BookInfo
import com.example.librai.data.repository.BookRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class BookViewModel : ViewModel() {
    private val repository = BookRepository(FirebaseFirestore.getInstance())

    private val _bookInfo = MutableStateFlow<BookInfo?>(null)
    val bookInfo: StateFlow<BookInfo?> = _bookInfo

    fun loadBookInfo(isbn: String) {
        viewModelScope.launch {
            val info = repository.fetchBookInfo(isbn)
            _bookInfo.value = info
        }
    }
}