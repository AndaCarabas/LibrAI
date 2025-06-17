package com.example.librai.viewmodel

import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModel
import com.example.librai.models.Book
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class BookDetailViewModel (
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) : ViewModel() {

    private val _book = MutableStateFlow<Book?>(null)
    val book: StateFlow<Book?> = _book

    private val userId get() = auth.currentUser?.uid ?: error("Unauthenticated")

    fun loadBook(bookId: String) {
        firestore.collection("users")
            .document(userId)
            .collection("books")
            .document(bookId)
            .addSnapshotListener { snap, _ ->
                _book.value = snap?.toObject(Book::class.java)
            }
    }

    fun deleteBook(bookId: String, onComplete: (Boolean) -> Unit) {
        firestore.collection("users")
            .document(userId)
            .collection("books")
            .document(bookId)
            .delete()
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }

}