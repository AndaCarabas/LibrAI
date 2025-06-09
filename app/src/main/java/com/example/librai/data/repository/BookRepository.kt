package com.example.librai.data.repository

import com.example.librai.models.Book
import com.example.librai.models.User
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class BookRepository (private val firestore: FirebaseFirestore) {

    fun getUserBooksRef(userId: String) =
        firestore.collection("users").document(userId).collection("books")

    suspend fun addBook(userId: String, book: Book): Result<Unit> {
        return try {
            val bookId = getUserBooksRef(userId).document().id
            val bookWithId = book.copy(id = bookId)
            getUserBooksRef(userId).document(bookId).set(bookWithId).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getBooks(userId: String): Result<List<Book>> {
        return try {
            val snapshot = getUserBooksRef(userId).get().await()
            val books = snapshot.toObjects(Book::class.java)
            Result.success(books)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}