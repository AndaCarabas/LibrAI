package com.example.librai.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.CenterFocusStrong
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.librai.ui.navigation.BottomNavBar
import com.example.librai.ui.theme.HighlightColor
import com.example.librai.ui.theme.PrimaryColor
import com.example.librai.R
import com.example.librai.ui.components.RecommendationsSection
import com.example.librai.viewmodel.HomeViewModel
import com.example.librai.viewmodel.HomeViewModelFactory
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController) {

    val auth = remember { FirebaseAuth.getInstance() }
    val fs = remember { FirebaseFirestore.getInstance() }
    val viewModel: HomeViewModel = viewModel(
        factory = HomeViewModelFactory(auth, fs)
    )
    val userName by viewModel.userName.collectAsState()
    val totalBooks by viewModel.totalBooks.collectAsState()
    val addedThisMonth by viewModel.addedThisMonth.collectAsState()
    val recentBooks by viewModel.recentBooks.collectAsState()
    val quoteOfTheDay by viewModel.quoteOfTheDay.collectAsState()
    val quoteText by viewModel.quoteText.collectAsState()
    val quoteAuthor by viewModel.quoteAuthor.collectAsState()
    var showSheet by remember { mutableStateOf(false) }
    val readingBooks by viewModel.currentlyReading.collectAsState()
    val toReadBooks by viewModel.toReadList.collectAsState()
    val readBooks by viewModel.readList.collectAsState()
    val isLoadingRecs by viewModel.isLoadingRecs.collectAsState()
    val recs by viewModel.personalizedRecs.collectAsState()

    Scaffold(
        containerColor = PrimaryColor,
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
                currentRoute = "home",
                onItemSelected = { route ->
                    if (route != "home") navController.navigate(route)
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(innerPadding)
        ) {
            Surface(
                color = PrimaryColor,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            ) {
                Box(Modifier.fillMaxSize().padding(16.dp)) {
                    IconButton(
                        onClick = {
                            viewModel.signOut()
                            navController.navigate("auth") {
                                popUpTo(0); launchSingleTop = true
                            }
                        },
                        modifier = Modifier.align(Alignment.TopEnd)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ExitToApp, "Logout", tint = Color.White)
                    }
                    Text(
                        "Hello, $userName!",
                        style = MaterialTheme.typography.headlineLarge,
                        color = Color.White,
                        modifier = Modifier
                            .padding(horizontal = 16.dp, vertical = 16.dp)
                            .offset(y = 30.dp)
                            .align(Alignment.TopStart)
                    )
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .offset(y = 150.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    label = "Total Books",
                    value = totalBooks.toString(),
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    label = "Books Added\nThis Month",
                    value = addedThisMonth.toString(),
                    modifier = Modifier.weight(1f)
                )
            }
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 280.dp)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp)
            ) {
                Text(
                    "Quote of the Day",
                    style = MaterialTheme.typography.titleMedium,
                    color = PrimaryColor
                )
                Spacer(Modifier.height(8.dp))
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text(
                            "“$quoteText”",
                            style = MaterialTheme.typography.bodyLarge,
                            color = PrimaryColor
                        )
                        Text(
                            "- ${quoteAuthor}",
                            style = MaterialTheme.typography.bodySmall,
                            color = PrimaryColor,
                            modifier = Modifier.align(Alignment.End)
                        )
                    }
                }

                Spacer(Modifier.height(24.dp))
                Text(
                    "Recently Added",
                    style = MaterialTheme.typography.titleMedium,
                    color = PrimaryColor
                )
                Spacer(Modifier.height(8.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp)
                ) {
                    items(recentBooks) { book ->
                        AsyncImage(
                            model = book.coverUrl,
                            contentDescription = book.title,
                            placeholder = painterResource(R.drawable.book_placeholder),
                            error = painterResource(R.drawable.book_placeholder),
                            fallback = painterResource(R.drawable.book_placeholder),
                            modifier = Modifier
                                .size(100.dp, 150.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { navController.navigate("bookDetail/${book.id}") },
                            contentScale = ContentScale.Crop
                        )
                    }
                }
                Spacer(Modifier.height(24.dp))

                Text(
                    "Currently Reading",
                    style = MaterialTheme.typography.titleMedium,
                    color = PrimaryColor
                )
                Spacer(Modifier.height(8.dp))
                when {
                    readingBooks.isNotEmpty() -> {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            items(readingBooks) { book ->
                                AsyncImage(
                                    model = book.coverUrl,
                                    contentDescription = book.title,
                                    placeholder = painterResource(R.drawable.book_placeholder),
                                    error = painterResource(R.drawable.book_placeholder),
                                    fallback = painterResource(R.drawable.book_placeholder),
                                    modifier = Modifier
                                        .size(100.dp, 150.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable { navController.navigate("bookDetail/${book.id}") },
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }
                    }

                    toReadBooks.isNotEmpty() -> {
                        Text(
                            "You’re not reading anything right now. How about your “To Read” list?",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Black
                        )
                        Spacer(Modifier.height(8.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            items(toReadBooks) { book ->
                                AsyncImage(
                                    model = book.coverUrl,
                                    contentDescription = book.title,
                                    placeholder = painterResource(R.drawable.book_placeholder),
                                    error = painterResource(R.drawable.book_placeholder),
                                    fallback = painterResource(R.drawable.book_placeholder),
                                    modifier = Modifier
                                        .size(100.dp, 150.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable { navController.navigate("bookDetail/${book.id}") },
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }
                    }

                    else -> {
                        Text(
                            "Looks like you haven’t added any books yet. Try getting some recommendations!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Black
                        )
                    }
                }
                Spacer(Modifier.height(24.dp))
                RecommendationsSection(
                    recs      = recs,
                    isLoading = isLoadingRecs,
                    onFetch   = { viewModel.fetchPersonalizedRecs(readBooks) }
                )
                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun StatCard(label: String, value: String, modifier: Modifier = Modifier) {
    Card(
        shape = RoundedCornerShape(12.dp),
        modifier = modifier.height(100.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(value, style = MaterialTheme.typography.headlineMedium, color = PrimaryColor)
            Text(label, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
        }
    }
}

