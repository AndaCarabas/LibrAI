package com.example.librai.viewmodel

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.librai.models.BookInfo
import com.example.librai.data.repository.BookRepository
import com.example.librai.models.Book
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.File

class BookFormViewModel (private val repository: BookRepository) : ViewModel() {


    var bookTitle by mutableStateOf("")
    var bookAuthor by mutableStateOf("")
    var books = mutableStateListOf<Book>()
    var coverUrl by mutableStateOf<String?>(null)
    var imageUri by mutableStateOf<Uri?>(null)
    var errorMessage by mutableStateOf<String?>(null)
    private val _userUid = mutableStateOf<String?>(null)
    val userUid: State<String?> = _userUid

    private val _bookInfo = MutableStateFlow<BookInfo?>(null)
    val bookInfo: StateFlow<BookInfo?> = _bookInfo

    var bookNotFound by mutableStateOf(false)

    fun loadBookInfo(isbn: String) {
        viewModelScope.launch {
            val result = repository.fetchBookInfo(isbn)
            if (result != null) {
                _bookInfo.value = result
                bookTitle = result.title
                bookAuthor = result.author
                coverUrl = result.coverUrl
            } else {
                bookNotFound = true
            }
        }
    }
//    fun saveBook(userId: String) {
//        val newBook = Book(
//            title = bookTitle.trim(),
//            author = bookAuthor.trim(),
//            coverUrl = imageUri?.toString() ?: coverUrl
//        )
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

    fun saveBook(onResult: (Boolean) -> Unit) {
        val book = Book(
            title = bookTitle.trim(),
            author = bookAuthor.trim(),
            coverUrl = imageUri?.toString() ?: coverUrl
        )
        repository.saveBook(book, onResult)
    }



    @Composable
    fun pickImageLauncher(): ActivityResultLauncher<String> =
        rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let { imageUri = it }
        }

    @Composable
    fun takePhotoLauncher(
        context: Context
    ): Pair<ActivityResultLauncher<Uri>, () -> Uri> {
        val photoFile = File.createTempFile("book_cover_", ".jpg", context.cacheDir)
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            photoFile
        )
        imageUri = uri
        return Pair(
            rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
                if (!success) imageUri = null
            },
            { uri }
        )
    }
}