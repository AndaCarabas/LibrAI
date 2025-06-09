package com.example.librai.data.repository

import android.util.Log
import com.example.librai.models.Book
import com.example.librai.models.BookInfo
import com.example.librai.models.User
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.serialization.json.*

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

    suspend fun fetchBookInfo(isbn: String): BookInfo? {

        val url = "https://www.googleapis.com/books/v1/volumes?q=isbn:${isbn}"
        val client = HttpClient(CIO)

        return try {
            val response: String = client.get(url).bodyAsText()
            val json = Json.parseToJsonElement(response).jsonObject
            val volumeInfo = json["items"]?.jsonArray?.get(0)
                ?.jsonObject?.get("volumeInfo")?.jsonObject

            Log.d("BookAPI", "Request: ${url}")
            Log.d("BookAPI", "Response: ${response}")
            BookInfo(
                title = volumeInfo?.get("title")?.jsonPrimitive?.content ?: "No title",
                author = volumeInfo?.get("authors")?.jsonArray?.getOrNull(0)?.jsonPrimitive?.content ?: "Unknown",
                coverUrl = volumeInfo?.get("imageLinks")?.jsonObject?.get("thumbnail")?.jsonPrimitive?.content?.replace("http://", "https://") ?: ""
            )
        } catch (e: Exception) {
            null
        } finally {
            client.close()
        }
    }
}