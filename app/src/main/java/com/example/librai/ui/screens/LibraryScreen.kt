package com.example.librai.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Button
import androidx.compose.ui.unit.dp
import com.example.librai.viewmodel.LibraryViewModel

@Composable
fun LibraryScreen(viewModel: LibraryViewModel, userId: String) {
    val books = viewModel.books
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.loadBooks(userId)
    }

    Column(modifier = Modifier.padding(16.dp)) {
        Text("Your Library", style = MaterialTheme.typography.headlineSmall)

        OutlinedTextField(
            value = viewModel.bookTitle,
            onValueChange = { viewModel.bookTitle = it },
            label = { Text("Title") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = viewModel.bookAuthor,
            onValueChange = { viewModel.bookAuthor = it },
            label = { Text("Author") },
            modifier = Modifier.fillMaxWidth()
        )

        Button(onClick = { viewModel.addBook(userId) }, modifier = Modifier.fillMaxWidth()) {
            Text("Add Book")
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn {
            items(books) { book ->
                Text("${book.title} by ${book.author}")
            }
        }

        viewModel.errorMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.errorMessage = null
        }
    }
}