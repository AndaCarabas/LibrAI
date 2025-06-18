package com.example.librai.ui.screens

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.librai.data.repository.BookRepository
import com.example.librai.ui.theme.PrimaryColor
import com.example.librai.viewmodel.BookDetailViewModel
import com.example.librai.viewmodel.BookDetailViewModelFactory
import com.example.librai.viewmodel.BookFormViewModel
import com.example.librai.viewmodel.BookFormViewModelFactory
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookDetailScreen(
    bookId: String,
    navController: NavController,
) {
    val context = LocalContext.current
    val firestore = remember { FirebaseFirestore.getInstance() }
    val auth = remember { FirebaseAuth.getInstance() }
    val repository = remember { BookRepository(firestore, auth) }

    val viewModel: BookDetailViewModel = viewModel(
        factory = BookDetailViewModelFactory(firestore,auth)
    )

    val book by viewModel.book.collectAsState()
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var showSummaryDialog by remember { mutableStateOf(false) }
    var showSimilarDialog by remember { mutableStateOf(false) }
    var showPersonalDialog by remember { mutableStateOf(false) }
    var userNotes by remember { mutableStateOf("") }

    // Load when screen is shown
    LaunchedEffect(bookId) {
        viewModel.loadBook(bookId)
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title   = { Text("Delete this book?") },
            text    = { Text("This action cannot be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteConfirm = false
                    viewModel.deleteBook(bookId) { success ->
                        if (success) navController.popBackStack("library", false)
                        else Toast.makeText(context, "Delete failed", Toast.LENGTH_SHORT).show()
                    }
                }) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Cancel")
                }
            },
            containerColor = Color.White,
            tonalElevation = 0.dp,
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                title = { /* empty—we’ll draw our own below */ },
                actions = {
                    IconButton(onClick = {
                        // Navigate to your edit form, passing bookId
                        navController.navigate("bookForm?bookId=${bookId}")
                    }) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit")
                    }
                    IconButton(onClick = { showDeleteConfirm = true }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor              = PrimaryColor,
                    navigationIconContentColor  = Color.White,
                    actionIconContentColor      = Color.White,
                    titleContentColor           = Color.White
                )
            )
        },
        containerColor = PrimaryColor,
        contentColor   = Color.White
    ) { inner ->
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(inner)
        ) {
            book?.let { b ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                ) {
                    // Cover
                    AsyncImage(
                        model = b.coverUrl,
                        contentDescription = "Cover",
                        modifier = Modifier
                            .fillMaxWidth(0.6f)
                            .aspectRatio(0.7f)
                            .align(Alignment.CenterHorizontally)
                            .padding(top = 16.dp),
                        contentScale = ContentScale.Fit
                    )
                    Spacer(Modifier.height(24.dp))
                    // Title / Author
                    Text(
                        text = b.title,
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color.White,
                        //modifier = Modifier.align(Alignment.CenterHorizontally).padding(16.dp)
                        modifier  = Modifier
                            .fillMaxWidth(),                  // <-- take up all the available width
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = b.author,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.8f),
                        //modifier = Modifier.align(Alignment.CenterHorizontally).padding(16.dp)
                        modifier  = Modifier
                            .fillMaxWidth(),                  // <-- take up all the available width
                        textAlign = TextAlign.Center
                    )
                    // White “card” for description + buttons
                    Surface(
                        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                        color = Color.White,
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight()
                            .padding(top = 16.dp)
                    ) {
                        Column(modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp)
                        ) {
                            Text("Description", style = MaterialTheme.typography.titleMedium)
                            Spacer(Modifier.height(8.dp))
                            Text(b.description ?: "No description available.", style = MaterialTheme.typography.bodySmall)
                            if (b.categories.isNotEmpty()) {
                                Text("Categories: " + b.categories.joinToString(", "))
                            }
                            Text("Status: ${b.status}")
                            b.notes?.let {
                                Text("Notes: $it")
                            }

                            Spacer(Modifier.height(24.dp))
                            // Your three action buttons
                            Button(
                                onClick = {
                                    viewModel.fetchSummary(b)
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor)
                            ) {
                                if (viewModel.isLoadingSummary) {
                                    CircularProgressIndicator(color=Color.White, modifier=Modifier.size(16.dp))
                                    Spacer(Modifier.width(8.dp))
                                }
                                Text("Get Summary")
                            }
                            LaunchedEffect(viewModel.isLoadingSummary) {
                                if (!viewModel.isLoadingSummary && viewModel.summaryText != null) {
                                    showSummaryDialog = true
                                }
                            }
                            Spacer(Modifier.height(12.dp))
                            Button(
                                onClick = {
                                    viewModel.fetchSimilar(b)
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor)
                            ) {
                                if (viewModel.isLoadingSimilar) {
                                    CircularProgressIndicator(color=Color.White, modifier=Modifier.size(16.dp))
                                    Spacer(Modifier.width(8.dp))
                                }
                                Text("Find Similar Books")
                            }
                            LaunchedEffect(viewModel.isLoadingSimilar) {
                                if (!viewModel.isLoadingSummary && !viewModel.similarBooks.isEmpty()) {
                                    showSimilarDialog = true
                                }
                            }
                            Spacer(Modifier.height(10.dp))
                            OutlinedTextField(
                                value = userNotes,
                                onValueChange = { userNotes = it },
                                label = { Text("What did you love about this book?") },
                                placeholder = { Text("e.g. slow-burn romance, witty banter…") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp)
                            )
                            Spacer(Modifier.height(12.dp))
                            Button(
                                onClick = {
                                    viewModel.fetchContextualRecs(b, userNotes)
                                },
                                enabled = !viewModel.isLoadingPersonalized,
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor)
                            ) {
                                if (viewModel.isLoadingPersonalized) {
                                    CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White)
                                    Spacer(Modifier.width(8.dp))
                                }
                                Text("Get Personalized Recs")
                            }
                            LaunchedEffect(viewModel.isLoadingPersonalized) {
                                if (!viewModel.personalizedRecs.isEmpty() && !viewModel.isLoadingPersonalized) {
                                    showPersonalDialog = true
                                }
                            }
                            Spacer(Modifier.height(12.dp))
                            Button(
                                onClick = { /* Intent to browser or ecommerce */ },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor)
                            ) {
                                Text("Buy Book")
                            }
                        }
                    }
                }
            } ?: run {
                // loading or “not found”
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color.White)
                }
            }
        }
    }
    // — Summary Dialog —
    if (showSummaryDialog) {
        AlertDialog(
            onDismissRequest = { showSummaryDialog = false },
            title   = { Text("Summary") },
            text    = { Text(viewModel.summaryText ?: "No summary available.") },
            confirmButton = {
                TextButton(onClick = { showSummaryDialog = false }) {
                    Text("OK")
                }
            },
            containerColor = Color.White,
            tonalElevation = 0.dp,
        )
    }
    // — Similar Books Dialog —
    if (showSimilarDialog) {
        AlertDialog(
            onDismissRequest = { showSimilarDialog = false },
            title   = { Text("Similar Books") },
            text    = {
                Column {
                    if (viewModel.similarBooks.isEmpty()) {
                        Text("No recommendations found.")
                    } else {
                        viewModel.similarBooks.forEach { info ->
                            Text("• ${info.title} by ${info.author}")
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showSimilarDialog = false }) {
                    Text("Close")
                }
            },
            containerColor = Color.White,
            tonalElevation = 0.dp,
        )
    }

    if (showPersonalDialog) {
        AlertDialog(
            onDismissRequest = { showPersonalDialog = false },
            title   = { Text("Recommended for You") },
            text    = {
                if (viewModel.personalizedRecs.isEmpty() && !viewModel.isLoadingPersonalized) {
                    Text("No recommendations found ${viewModel.personalizedRecs.isEmpty()}.")
                } else {
                    Column {
                        viewModel.personalizedRecs.forEach { info ->
                            Text("• ${info.title} by ${info.author}")
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showPersonalDialog = false }) {
                    Text("Close")
                }
            },
            containerColor = Color.White,
            tonalElevation = 0.dp,
        )
    }

}
