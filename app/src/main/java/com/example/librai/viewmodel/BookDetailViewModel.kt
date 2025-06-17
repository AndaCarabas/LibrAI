package com.example.librai.viewmodel

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.librai.models.Book
import com.example.librai.models.BookInfo
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.addJsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import com.example.librai.BuildConfig
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.JsonPrimitive

class BookDetailViewModel (
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
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

    // Summary state
    var summaryText by mutableStateOf<String?>(null)
    var isLoadingSummary by mutableStateOf(false)

    // Similar-books state
    var similarBooks by mutableStateOf<List<BookInfo>>(emptyList())
    var isLoadingSimilar by mutableStateOf(false)

    private val openAiKey = BuildConfig.OPENAI_API_KEY

    val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                prettyPrint        = false
            })
        }
    }

    fun fetchSummary(book: Book) {
        viewModelScope.launch {
            isLoadingSummary = true
            try {
                val prompt = """
            Please provide a concise summary of the following book:
            Title: ${book.title}
            Author: ${book.author}
            Description: ${book.description.orEmpty()}
          """.trimIndent()


                val response = client.post("https://api.openai.com/v1/chat/completions") {
                    header("Authorization", "Bearer $openAiKey")
                    contentType(ContentType.Application.Json)
                    setBody(
                        buildJsonObject {
                            put("model",  JsonPrimitive("gpt-3.5-turbo"))
                            putJsonArray("messages") {
                                addJsonObject {
                                    put("role", JsonPrimitive("user"))
                                    put("content", JsonPrimitive(prompt))
                                }
                            }
                        }
                    )
                }.body<JsonObject>()

                Log.d("BookAPI", "****Response: ${response}")

                val content = response["choices"]!!
                    .jsonArray[0].jsonObject["message"]!!
                    .jsonObject["content"]!!.jsonPrimitive.content

                summaryText = content.trim()
            } catch (e: Exception) {
                summaryText = "Failed to fetch summary: ${e.message}"
            } finally {
                isLoadingSummary = false
            }
        }
    }

    fun fetchSimilar(book: Book) {
        viewModelScope.launch {
            isLoadingSimilar = true
            try {
                val query = "intitle:${book.title}&inauthor:${book.author}"
                val url   = "https://www.googleapis.com/books/v1/volumes?q=$query&maxResults=5"

                val raw = client.get(url).body<String>()
                val json = Json.parseToJsonElement(raw).jsonObject

                val items = json["items"]?.jsonArray.orEmpty()
                similarBooks = items.mapNotNull { item ->
                    val info = item.jsonObject["volumeInfo"]?.jsonObject ?: return@mapNotNull null
                    BookInfo(
                        title    = info["title"]?.jsonPrimitive?.content ?: return@mapNotNull null,
                        author  = info["authors"]?.jsonArray?.getOrNull(0)?.jsonPrimitive?.content ?: "Unknown",
                        coverUrl = info["imageLinks"]
                            ?.jsonObject
                            ?.get("thumbnail")
                            ?.jsonPrimitive
                            ?.content
                            ?.replace("http://","https://").toString()
                    )
                }
            } catch (e: Exception) {
                similarBooks = emptyList()
            } finally {
                isLoadingSimilar = false
            }
        }
    }

}