package com.example.librai.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.librai.data.repository.BookRepository
import com.example.librai.models.Book
import kotlinx.coroutines.launch
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class LibraryViewModel (private val repository: BookRepository) : ViewModel() {

    var bookTitle by mutableStateOf("")
    var bookAuthor by mutableStateOf("")
   // var books = mutableStateListOf<Book>()
    var errorMessage by mutableStateOf<String?>(null)
    private val _userUid = mutableStateOf<String?>(null)
    val userUid: State<String?> = _userUid

    private val _books = MutableStateFlow<List<Book>>(emptyList())
    val books: StateFlow<List<Book>> = _books

//    fun loadBooks(userId: String) {
//        viewModelScope.launch {
//            val result = repository.getBooks(userId)
//            if(result.isSuccess){
//                books.clear().also { books.addAll(result.getOrNull().orEmpty()) }
//            }
//            else{
//                errorMessage = result.exceptionOrNull()?.message
//            }
//
//        }
//    }

    fun loadBooks(userId: String) {
        FirebaseFirestore.getInstance()
            .collection("users")
            .document(userId)
            .collection("books")
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    val result = snapshot.documents.mapNotNull { it.toObject(Book::class.java) }
                    _books.value = result
                }
            }
    }
//    fun addBook(userId: String) {
//        val newBook = Book(title = bookTitle.trim(), author = bookAuthor.trim())
//        viewModelScope.launch {
//            val result = repository.addBook(userId, newBook)
//            if(result.isSuccess){
//                books.add(newBook)
//                bookTitle = ""
//                bookAuthor = ""
//            }
//            else{
//                errorMessage = result.exceptionOrNull()?.message
//            }
//        }
//    }
}