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
import io.ktor.client.statement.bodyAsText
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.intOrNull
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.text.Normalizer
import java.util.Locale

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
                val encodedTitle  = URLEncoder.encode(book.title, StandardCharsets.UTF_8.name())
                val encodedAuthor = URLEncoder.encode(book.author, StandardCharsets.UTF_8.name())
                val url = "https://www.googleapis.com/books/v1/volumes" +
                        "?q=intitle:$encodedTitle+inauthor:$encodedAuthor" +
                        "&maxResults=10"

                Log.d("BookAPI", "Similar URL → $url")

                val rawJson = client.get(url).bodyAsText()
                Log.d("BookAPI", "Similar raw JSON → $rawJson")

                val json       = Json.parseToJsonElement(rawJson).jsonObject
                val totalItems = json["totalItems"]?.jsonPrimitive?.intOrNull ?: 0
                Log.d("BookAPI", "Similar totalItems → $totalItems")

                val items = json["items"]?.jsonArray.orEmpty()
                if (totalItems == 0) {
                    similarBooks = emptyList()
                } else {
                    val all = items.mapNotNull { item ->
                        val info = item.jsonObject["volumeInfo"]?.jsonObject ?: return@mapNotNull null
                        BookInfo(
                            title    = info["title"]?.jsonPrimitive?.content ?: return@mapNotNull null,
                            author   = info["authors"]?.jsonArray
                                ?.joinToString(", ") { it.jsonPrimitive.content }
                                ?: "Unknown",
                            coverUrl = info["imageLinks"]?.jsonObject
                                ?.get("thumbnail")?.jsonPrimitive?.content
                                ?.replace("http://", "https://").toString()
                        )
                    }

                    fun normalizeKey(title: String, author: String): String {
                        val raw = "${title.trim()}|${author.trim()}"
                            .lowercase(Locale.getDefault())
                        // strip diacritics
                        return Normalizer.normalize(raw, Normalizer.Form.NFD)
                            .replace(Regex("\\p{M}"), "")
                    }
                    val origKey = normalizeKey(book.title, book.author)
                    val filtered = all.filter { bi ->
                        normalizeKey(bi.title, bi.author) != origKey
                    }

                    val seenTitles = mutableSetOf<String>()
                    val deduped = mutableListOf<BookInfo>()
                    for (book in filtered) {
                        val key = normalizeKey(book.title, book.author)
                        if (seenTitles.add(key)) {
                            // first time we see this title, keep it
                            Log.d("BookAPI", "Title and author: → $key")
                            deduped += book
                        }
                        if (deduped.size >= 5) break    // only want up to 5
                    }
                    similarBooks = deduped
                }
            } catch (e: Exception) {
                Log.e("BookAPI", "Error fetching similar", e)
                similarBooks = emptyList()
            } finally {
                isLoadingSimilar = false
            }
        }
    }

    var personalizedRecs by mutableStateOf<List<BookInfo>>(emptyList())
    var isLoadingPersonalized by mutableStateOf(false)

    fun fetchContextualRecs(book: Book, userNotes: String) {
        viewModelScope.launch {
            isLoadingPersonalized = true
            try {
                val prompt = buildString {
                    appendLine("You’re an expert librarian. A user read “${book.title}” by ${book.author}.")
                    book.description?.let { appendLine("Description: $it") }
                    appendLine("User’s feedback: $userNotes")
                    appendLine()
                    appendLine("Based on this, recommend 5 other books (title and author) that match the themes or style.")
                    appendLine("Return the result as a JSON array of objects with “title” and “author” fields.")
                }

                val response = client.post("https://api.openai.com/v1/chat/completions") {
                    header("Authorization","Bearer $openAiKey")
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
                            put("max_tokens", JsonPrimitive(200))
                        })
                }.body<JsonObject>()

                Log.d("BookAPI", "Response:  → $response")

                // parse response["choices"][0]["message"]["content"] as JSON
                val content = response["choices"]!!
                    .jsonArray[0].jsonObject["message"]!!
                    .jsonObject["content"]!!.jsonPrimitive.content

                Log.d("BookAPI", "Response content:  → $content")

                //extract the JSON array substring
                val start = content.indexOfFirst { it == '[' }
                val end   = content.indexOfLast  { it == ']' }

                val jsonArrayText = if (start != -1 && end != -1 && end > start) {
                    content.substring(start, end + 1)
                } else {
                    Log.e("BookAPI", "Could not find JSON array in LLM response")
                    "[]"
                }

                Log.d("BookAPI", "Extracted JSON →\n$jsonArrayText")

                personalizedRecs = try {
                    Json.decodeFromString(jsonArrayText)
                } catch (e: Exception) {
                    Log.e("BookAPI", "JSON parse error", e)
                    emptyList()
                }
            } catch (e: Exception) {
                personalizedRecs = emptyList()
            } finally {
                isLoadingPersonalized = false
            }
        }
    }

}