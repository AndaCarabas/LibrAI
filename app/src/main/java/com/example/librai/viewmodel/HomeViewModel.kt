package com.example.librai.viewmodel

import android.app.Application
import android.util.Log
import androidx.compose.ui.platform.LocalContext
import io.ktor.http.contentType
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.librai.BuildConfig
import com.example.librai.models.Book
import com.example.librai.models.BookInfo
import com.example.librai.models.Quote
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.addJsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.putJsonArray
import java.util.Calendar

class HomeViewModel (
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : ViewModel() {
    private val _userName       = MutableStateFlow<String>("")
    val userName: StateFlow<String>        = _userName

    private val _totalBooks     = MutableStateFlow(0)
    val totalBooks: StateFlow<Int>         = _totalBooks

    private val _addedThisMonth = MutableStateFlow(0)
    val addedThisMonth: StateFlow<Int>     = _addedThisMonth

    private val _recentBooks    = MutableStateFlow<List<Book>>(emptyList())
    val recentBooks: StateFlow<List<Book>> = _recentBooks

    private val _quoteOfTheDay  = MutableStateFlow<String>("")
    val quoteOfTheDay: StateFlow<String>   = _quoteOfTheDay

    private val _quoteText  = MutableStateFlow<String>("")
    val quoteText: StateFlow<String>   = _quoteText

    private val _quoteAuthor  = MutableStateFlow<String>("")
    val quoteAuthor: StateFlow<String>   = _quoteAuthor

    private val _currentlyReading   = MutableStateFlow<List<Book>>(emptyList())
    val currentlyReading: StateFlow<List<Book>> = _currentlyReading

    private val _toReadList        = MutableStateFlow<List<Book>>(emptyList())
    val toReadList: StateFlow<List<Book>> = _toReadList

    private val _readList        = MutableStateFlow<List<Book>>(emptyList())
    val readList: StateFlow<List<Book>> = _readList

    private val _isLoadingRecs = MutableStateFlow(false)
    val isLoadingRecs: StateFlow<Boolean> = _isLoadingRecs

    private val _personalizedRecs = MutableStateFlow<List<BookInfo>>(emptyList())
    val personalizedRecs: StateFlow<List<BookInfo>> = _personalizedRecs

    private val openAiKey = BuildConfig.OPENAI_API_KEY

    val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                prettyPrint        = false
            })
        }
    }
    private val quotesJson = """
[
  {
    "q": "A self that goes on changing is a self that goes on living.",
    "a": "Virginia Woolf"
  },
  {
    "q": "So many books, so little time.",
    "a": "Frank Zappa"
  },
  {
    "q": "There is no friend as loyal as a book.",
    "a": "Ernest Hemingway"
  },
  {
    "q": "I have always imagined that Paradise will be a kind of library.",
    "a": "Jorge Luis Borges"
  },
  {
    "q": "When in doubt, go to the library.",
    "a": "J.K. Rowling"
  },
  {
    "q": "Reading is a discount ticket to everywhere.",
    "a": "Mary Schmich"
  },
  {
    "q": "Books are a uniquely portable magic.",
    "a": "Stephen King"
  },
  {
    "q": "The only thing you absolutely have to know is the location of the library.",
    "a": "Albert Einstein"
  },
  {
    "q": "I cannot live without books.",
    "a": "Thomas Jefferson"
  },
  {
    "q": "A room without books is like a body without a soul.",
    "a": "Marcus Tullius Cicero"
  },
  {
    "q": "Never trust anyone who has not brought a book with them.",
    "a": "Lemony Snicket"
  },
  {
    "q": "We read to know we are not alone.",
    "a": "C.S. Lewis"
  }
]

"""

    val quotes: List<Quote> = Json.decodeFromString(quotesJson)


    private var booksListener: ListenerRegistration? = null


    init {
        auth.currentUser?.let { user ->
            _userName.value = auth.currentUser?.displayName
                ?: auth.currentUser?.email?.substringBefore("@").orEmpty()

            val booksRef = firestore
                .collection("users")
                .document(user.uid)
                .collection("books")

            booksListener = booksRef.addSnapshotListener { snap, error ->
                if (error != null || snap == null) return@addSnapshotListener

                val all = snap.documents.mapNotNull { it.toObject(Book::class.java) }

                _totalBooks.value     = all.size
                _currentlyReading.value   = all.filter { it.status == "Reading" }
                _toReadList.value         = all.filter { it.status == "To Read" }
                _readList.value         = all.filter { it.status == "Read" }


                try {
                    val startOfMonth = Calendar.getInstance().apply {
                        set(Calendar.DAY_OF_MONTH, 1)
                        set(Calendar.HOUR_OF_DAY, 0)
                        set(Calendar.MINUTE, 0)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }.timeInMillis

                    _addedThisMonth.value = all.count { it.timestamp >= startOfMonth }

                    _recentBooks.value = all
                        .sortedByDescending { it.timestamp }
                        .take(5)
                } catch (_: Exception) {
                }
            }


            val day = Calendar.getInstance().get(Calendar.DAY_OF_YEAR)
            val todayQuote = quotes[day % quotes.size]
            _quoteOfTheDay.value = "“${todayQuote.q}” — ${todayQuote.a}"

            _quoteText.value   = todayQuote.q
            _quoteAuthor.value = todayQuote.a
        }

    }
    override fun onCleared() {
        super.onCleared()
        booksListener?.remove()
    }

    fun fetchPersonalizedRecs(readBooks: List<Book>) {
        viewModelScope.launch {
            _isLoadingRecs.value = true
            try {
                // 1) build a prompt listing each read book
                val prompt = buildString {
                    appendLine("You’re an expert librarian. The user has already read the following books:")
                    readBooks.forEach { b ->
                        appendLine("- “${b.title}” by ${b.author}")
                    }
                    appendLine()
                    appendLine("Based on these, recommend 5 other books (title and author) that they might like.")
                    appendLine("Return the result as a JSON array of objects with \"title\" and \"author\" fields.")
                }

                // 2) call the OpenAI Chat API
                val response = client.post("https://api.openai.com/v1/chat/completions") {
                    header("Authorization", "Bearer $openAiKey")
                    contentType(ContentType.Application.Json)
                    setBody(buildJsonObject {
                        put("model",    JsonPrimitive("gpt-3.5-turbo"))
                        put("max_tokens", JsonPrimitive(200))
                        putJsonArray("messages") {
                            addJsonObject {
                                put("role",    JsonPrimitive("user"))
                                put("content", JsonPrimitive(prompt))
                            }
                        }
                    })
                }.body<JsonObject>()

                Log.d("BookAPI", "Response:  → $response")

                val raw = response["choices"]!!.jsonArray[0]
                    .jsonObject["message"]!!
                    .jsonObject["content"]!!.jsonPrimitive.content

                val start = raw.indexOfFirst { it == '[' }
                val end   = raw.indexOfLast  { it == ']' }
                val jsonArrayText = if (start != -1 && end != -1 && end > start) {
                    raw.substring(start, end + 1)
                } else {
                    Log.e("BookAPI", "Could not find JSON array in LLM response")
                    "[]"
                }
                Log.d("BookAPI", "Response content:  → $raw")

                val personalizedRecsTry = try {
                    Json.decodeFromString<List<BookInfo>>(jsonArrayText)
                } catch (e: Exception) {
                    Log.e("BookAPI", "JSON parse error", e)
                    emptyList()
                }
                _personalizedRecs.value = personalizedRecsTry

                Log.d("BookAPI", "Personalized:  → ${_personalizedRecs.value.toString()}")

            } catch (e: Exception) {
                Log.e("BookAPI", "Error fetching personalized recs", e)
                _personalizedRecs.value = emptyList()
            } finally {
                _isLoadingRecs.value = false
            }
        }
    }

    // 7) Sign out helper
    fun signOut() {
        auth.signOut()
    }
}