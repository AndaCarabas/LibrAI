package com.example.librai.ui.screens

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.librai.data.repository.BookRepository
import com.example.librai.ui.components.CameraPermissionHandler
import com.example.librai.viewmodel.BookFormViewModel
import com.example.librai.viewmodel.BookFormViewModelFactory
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.io.File

@Composable
fun BookFormScreen(
    isbn: String?,
    bookId: String?,
    navController: NavController
) {
    val context = LocalContext.current
    val firestore = remember { FirebaseFirestore.getInstance() }
    val auth = remember { FirebaseAuth.getInstance() }
    val repository = remember { BookRepository(firestore, auth) }

    val viewModel: BookFormViewModel = viewModel(
        factory = BookFormViewModelFactory(repository)
    )

    LaunchedEffect(bookId, isbn) {
        viewModel.initForm(bookId = bookId, isbn = isbn)
    }

    val photoFile = remember { File.createTempFile("book_", ".jpg", context.cacheDir) }
    val photoUri = remember {
        FileProvider.getUriForFile(context, "${context.packageName}.provider", photoFile)
    }

    val takePhotoLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) viewModel.imageUri = photoUri
    }

    val pickImageLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { viewModel.imageUri = it }
    }

    // Load from ISBN if available
    LaunchedEffect(isbn) {
        if (!isbn.isNullOrBlank()) viewModel.loadBookInfo(isbn)
    }

    if (viewModel.bookNotFound) {
        AlertDialog(
            onDismissRequest = { viewModel.bookNotFound = false },
            title = { Text("Book not found") },
            text = { Text("We couldn't retrieve book details for this ISBN. Would you like to scan again or fill the info manually?") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.bookNotFound = false
                    navController.popBackStack("scanner", inclusive = false)
                }) {
                    Text("Scan Again")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    viewModel.bookNotFound = false
                    // Stay on form screen
                }) {
                    Text("Add Manually")
                }
            } ,
            containerColor = Color.White,
            tonalElevation = 0.dp,
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Cover image preview
        AsyncImage(
            model = viewModel.imageUri  ?: viewModel.coverUrl,
            contentDescription = "Book Cover",
            modifier = Modifier
                .fillMaxWidth(0.6f) // 60% of screen width
                .aspectRatio(0.7f)
                .padding(8.dp)
                .clip(RoundedCornerShape(12.dp)),
            contentScale = ContentScale.Crop
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
            //horizontalArrangement = Arrangement.Center
        ) {
            Button(onClick = { pickImageLauncher.launch("image/*") }) {
                Text("Pick Image")
            }
            CameraPermissionHandler(
                rationaleMessage = "Camera permission is needed to take a photo of the book.",
                onPermissionGranted = {
                    Button(onClick = {
                        takePhotoLauncher.launch(photoUri)
                    }) {
                        Text("Take Photo")
                    }
                }
            )
        }

        Spacer(Modifier.height(16.dp))
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
        OutlinedTextField(
            value = viewModel.isbn.toString(),
            onValueChange = { viewModel.isbn = it },
            label = { Text("ISBN") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = viewModel.description.orEmpty(),
            onValueChange = { viewModel.description = it },
            label = { Text("Description") },
            modifier = Modifier.fillMaxWidth().height(120.dp),
            maxLines = 5
        )
        OutlinedTextField(
            value = viewModel.categoriesText,
            onValueChange = { viewModel.categoriesText = it },
            label = { Text("Categories (comma-separated)") },
            placeholder = { Text("e.g. Mystery, Classic") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))
        Text("Status", style = MaterialTheme.typography.bodyMedium)
        Row {
            listOf("To Read","Reading","Read").forEach { option ->
                Row(Modifier.padding(end = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = viewModel.status == option,
                        onClick  = { viewModel.status = option }
                    )
                    Text(option)
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = viewModel.notesText,
            onValueChange = { viewModel.notesText = it },
            label       = { Text("Notes") },
            placeholder = { Text("e.g. Lent to Alex on 06/22") },
            modifier    = Modifier
                .fillMaxWidth()
                .height(120.dp),
            maxLines = 5
        )


        // TODO Add more fields: publisher, description, etc.

        Spacer(Modifier.height(24.dp))
        Button(onClick = {
            viewModel.saveBook { success ->
                if (success) navController.popBackStack("library", false)
                else Toast.makeText(context, "Save failed", Toast.LENGTH_SHORT).show()
            }
        }) {
            Text(if (bookId != null) "Update Book" else "Save Book")
        }
    }
}