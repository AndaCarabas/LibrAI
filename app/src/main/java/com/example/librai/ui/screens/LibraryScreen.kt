package com.example.librai.ui.screens

import android.util.Log
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
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.Icon
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.MenuItemColors
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
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
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryFilters(
    authors: List<String>,
    categories: List<String>,
    onAuthorSelected: (String?) -> Unit,
    onCategorySelected: (String?) -> Unit
) {
    var filterMode by remember { mutableStateOf(0) }
    var selectedAuthor by remember { mutableStateOf<String?>(null) }
    var authorExpanded by remember { mutableStateOf(false) }

    var selectedCategory by remember { mutableStateOf<String?>(null) }
    var categoryExpanded by remember { mutableStateOf(false) }


    val modes = listOf("All", "Author", "Category")
    TabRow(selectedTabIndex = filterMode) {
        modes.forEachIndexed { i, title ->
            Tab(
                selected = filterMode == i,
                onClick   = { filterMode = i },
                text      = { Text(title) }
            )
        }
    }
    Spacer(Modifier.height(8.dp))

    when (filterMode) {
        1 -> {
            ExposedDropdownMenuBox(
                expanded        = authorExpanded,
                onExpandedChange= { authorExpanded = !authorExpanded }
            ) {
                OutlinedTextField(
                    modifier    = Modifier.fillMaxWidth(),
                    value       = selectedAuthor ?: "Select author",
                    onValueChange= {},
                    readOnly    = true,
                    label       = { Text("Author") },
                    trailingIcon= {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = authorExpanded)
                    },
                    colors      = TextFieldDefaults.colors()
                )
                ExposedDropdownMenu(
                    expanded        = authorExpanded,
                    onDismissRequest= { authorExpanded = false }
                ) {
                    DropdownMenuItem(
                        text    = { Text("Any author") },
                        onClick = {
                            selectedAuthor = null
                            onAuthorSelected(null)
                            authorExpanded = false
                        }
                    )
                    authors.forEach { author ->
                        DropdownMenuItem(
                            text    = { Text(author) },
                            onClick = {
                                selectedAuthor = author
                                onAuthorSelected(author)
                                authorExpanded = false
                            }
                        )
                    }
                }
            }
        }
        2 -> {
            ExposedDropdownMenuBox(
                expanded        = categoryExpanded,
                onExpandedChange= { categoryExpanded = !categoryExpanded }
            ) {
                OutlinedTextField(
                    modifier    = Modifier.fillMaxWidth(),
                    value       = selectedCategory ?: "Select category",
                    onValueChange= {},
                    readOnly    = true,
                    label       = { Text("Category") },
                    trailingIcon= {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded)
                    },
                    colors      = TextFieldDefaults.colors()
                )
                ExposedDropdownMenu(
                    expanded        = categoryExpanded,
                    onDismissRequest= { categoryExpanded = false }
                ) {
                    DropdownMenuItem(
                        text    = { Text("Any category") },
                        onClick = {
                            selectedCategory = null
                            onCategorySelected(null)
                            categoryExpanded = false
                        }
                    )
                    categories.forEach { cat ->
                        DropdownMenuItem(
                            text    = { Text(cat) },
                            onClick = {
                                selectedCategory = cat
                                onCategorySelected(cat)
                                categoryExpanded = false
                            }
                        )
                    }
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(navController: NavController,viewModel: LibraryViewModel, userId: String) {

    val booksState = viewModel.books.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    val context = LocalContext.current
    val systemUi = rememberSystemUiController()
    var showSheet by remember { mutableStateOf(false) }
    val allBooks by viewModel.books.collectAsState()

    // unique authors for the dropdown
    val authors = remember(allBooks) {
        allBooks.map { it.author }
            .distinct()
            .sorted()
    }

    Log.d("BookAPI", "***Authors : ${authors.joinToString()}")

// unique categories for the dropdown
// (books may have multiple categories each)
    val categories = remember(allBooks) {
        allBooks.flatMap { it.categories }
            .distinct()
            .sorted()
    }
    //Log.d("BookAPI", "***Categories : ${categories.joinToString()}")
    Log.d("BookAPI", "***Categories : ${categories}")

    var filterMode by remember { mutableStateOf(0) }
    // selected values (or null for “Any”)
    var authorExpanded   by remember { mutableStateOf(false) }
    var selectedAuthor   by remember { mutableStateOf<String?>(null) }

    var categoryExpanded by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        viewModel.loadBooks(userId)
    }
    LaunchedEffect(MintBackground) {
        systemUi.setNavigationBarColor(color = MintBackground)
    }

    val books = booksState.value



    Scaffold(
        containerColor = Color.White,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showSheet = true },
                containerColor = HighlightColor,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp),
                elevation = FloatingActionButtonDefaults.elevation(8.dp)
            ) {
                Icon(Icons.Default.CenterFocusStrong, contentDescription = "Scan")
            }
            if (showSheet) {
                AddBookOptionBottomSheet(
                    onScan = { navController.navigate("scanner") },
                    onManual = { navController.navigate("bookForm") },
                    onDismiss = { showSheet = false }
                )
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
                        placeholder = { Text("Search titles or authors", color = Color.Gray) },
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
                Column(modifier = Modifier.padding(16.dp)) {

                    // ➍ The TabRow
                    val modes = listOf("All", "Author", "Category")
                    TabRow(selectedTabIndex = filterMode, containerColor = Color.White) {
                        modes.forEachIndexed { i, title ->
                            Tab(
                                selected    = filterMode == i,
                                onClick     = {
                                    filterMode = i
                                    // reset selections when you switch modes:
                                    selectedAuthor   = null
                                    selectedCategory = null
                                },
                                text        = { Text(title) }
                            )
                        }
                    }
                    Spacer(Modifier.height(8.dp))

                    // ➎ Conditionally show the dropdown for Author or Category
                    when (filterMode) {
                        1 -> ExposedDropdownMenuBox(
                            expanded        = authorExpanded,
                            onExpandedChange= { authorExpanded = !authorExpanded },
                            modifier        = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value        = selectedAuthor ?: "Select author",
                                onValueChange= {},
                                readOnly     = true,
                                label        = { Text("Author") },
                                trailingIcon = {
                                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = authorExpanded)
                                },
                                modifier     = Modifier.fillMaxWidth().menuAnchor(),
                                colors       = TextFieldDefaults.colors(
                                    focusedContainerColor   = Color.White,
                                    unfocusedContainerColor = Color.White,
                                    focusedIndicatorColor   = Color.Black,
                                    unfocusedIndicatorColor = Color.Black,
                                    cursorColor             = AccentColor,
                                    focusedLeadingIconColor = AccentColor,
                                    unfocusedLeadingIconColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                                    focusedPlaceholderColor   = Color.Gray,
                                    unfocusedPlaceholderColor = Color.Gray.copy(alpha = 0.6f)
                                )
                            )
                            ExposedDropdownMenu(
                                expanded        = authorExpanded,
                                onDismissRequest= { authorExpanded = false },
                                containerColor = MintBackground
                            ) {
                                DropdownMenuItem(
                                    text    = { Text("Any author") },
                                    onClick = {
                                        selectedAuthor = null
                                        authorExpanded = false
                                    }
                                )
                                authors.forEach { author ->
                                    DropdownMenuItem(
                                        text    = { Text(author) },
                                        onClick = {
                                            selectedAuthor = author
                                            authorExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                        2 -> ExposedDropdownMenuBox(
                            expanded        = categoryExpanded,
                            onExpandedChange= { categoryExpanded = !categoryExpanded },
                            modifier        = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value        = selectedCategory ?: "Select category",
                                onValueChange= {},
                                readOnly     = true,
                                label        = { Text("Category") },
                                trailingIcon = {
                                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded)
                                },
                                modifier     = Modifier.fillMaxWidth().menuAnchor(),
                                colors       = TextFieldDefaults.colors(
                                    focusedContainerColor   = Color.White,
                                    unfocusedContainerColor = Color.White,
                                    focusedIndicatorColor   = Color.Black,
                                    unfocusedIndicatorColor = Color.Black,
                                    cursorColor             = AccentColor,
                                    focusedLeadingIconColor = AccentColor,
                                    unfocusedLeadingIconColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                                    focusedPlaceholderColor   = Color.Gray,
                                    unfocusedPlaceholderColor = Color.Gray.copy(alpha = 0.6f)
                                )
                            )
                            ExposedDropdownMenu(
                                expanded        = categoryExpanded,
                                onDismissRequest= { categoryExpanded = false },
                                containerColor = MintBackground
                            ) {
                                DropdownMenuItem(
                                    text    = { Text("Any category") },
                                    onClick = {
                                        selectedCategory = null
                                        categoryExpanded = false
                                    }
                                )
                                categories.forEach { cat ->
                                    DropdownMenuItem(
                                        text    = { Text(cat) },
                                        onClick = {
                                            selectedCategory = cat
                                            categoryExpanded = false
                                        },
                                    )
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(12.dp))
                    val postFilter = when {
                        selectedAuthor   != null -> allBooks.filter  { it.author == selectedAuthor }
                        selectedCategory != null -> allBooks.filter  { it.categories.contains(selectedCategory) }
                        else                     -> allBooks
                    }
                    val finalList = remember(postFilter, searchQuery) {
                        val filtered = if (searchQuery.isBlank()) postFilter
                        else postFilter.filter {
                            it.title.contains(searchQuery, ignoreCase=true) ||
                                    it.author.contains(searchQuery, ignoreCase=true)
                        }
                        filtered.sortedBy { it.title.lowercase(Locale.getDefault()) }
                    }

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(top = 16.dp)
                    ) {
                        items(finalList) { book ->
                            BookListItem(book) {
                                navController.navigate("bookDetail/${book.id}")
                            }
                        }
                        item { Spacer(modifier = Modifier.height(80.dp)) } // bottom padding
                    }
                }
            }
        }
    }
}