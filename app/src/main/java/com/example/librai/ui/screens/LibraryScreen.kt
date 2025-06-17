package com.example.librai.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CenterFocusStrong
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.Icon
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.librai.ui.components.BookListItem
import com.example.librai.ui.navigation.BottomNavBar
import com.example.librai.ui.theme.AccentColor
import com.example.librai.ui.theme.BackgroundColor
import com.example.librai.ui.theme.HighlightColor
import com.example.librai.ui.theme.MintBackground
import com.example.librai.ui.theme.PrimaryColor
import com.example.librai.viewmodel.LibraryViewModel
import com.google.accompanist.systemuicontroller.rememberSystemUiController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(navController: NavController,viewModel: LibraryViewModel, userId: String) {

    val booksState = viewModel.books.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    val context = LocalContext.current
    val systemUi = rememberSystemUiController()

    LaunchedEffect(Unit) {
        viewModel.loadBooks(userId)
    }
    LaunchedEffect(MintBackground) {
        systemUi.setNavigationBarColor(color = MintBackground)
    }


    Scaffold(
        containerColor = Color.White,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate("scanner") },
                containerColor = HighlightColor,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp),
                elevation = FloatingActionButtonDefaults.elevation(8.dp)
            ) {
                Icon(Icons.Default.CenterFocusStrong, contentDescription = "Scan")
            }
        },
        bottomBar = {
            BottomNavBar(
                currentRoute = "library",
                onItemSelected = { route ->
                    if (route != "library") navController.navigate(route)
                }
            )
        }
    ) { innerPadding ->
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
        ) {
            Surface(
                color = PrimaryColor,
                shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            ) {
                Column(
                    Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.Top
                ) {
                    Text(
                        "My Library",
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color.White
                    )
                    Spacer(Modifier.height(16.dp))
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = null, tint = AccentColor)
                        },
                        placeholder = { Text("Search books", color = Color.Gray) },
                        singleLine = true,
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor   = Color.White,
                            unfocusedContainerColor = Color.White,
                            focusedIndicatorColor   = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            cursorColor             = AccentColor,
                            focusedLeadingIconColor = AccentColor,
                            unfocusedLeadingIconColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                            focusedPlaceholderColor   = Color.Gray,
                            unfocusedPlaceholderColor = Color.Gray.copy(alpha = 0.6f)
                        ),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // 2) White list container overlapping the header
            Surface(
                color = Color.White,
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 160.dp)    // pulls it up to overlap the teal
            ) {
                val allBooks = booksState.value
                val filteredBooks = remember(allBooks, searchQuery) {
                    allBooks.filter { book ->
                        book.title.contains(searchQuery, ignoreCase = true) ||
                                book.author.contains(searchQuery, ignoreCase = true)
                    }
                }
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 16.dp)
                ) {
                    items(filteredBooks) { book ->
                        BookListItem(book) {
                            // navigate to detail or whatever
                        }
                    }
                    item { Spacer(modifier = Modifier.height(80.dp)) } // bottom padding
                }
            }
        }
    }
}