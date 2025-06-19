package com.example.librai.ui.screens

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import com.example.librai.viewmodel.ProfileViewModel
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.librai.data.repository.BookRepository
import com.example.librai.ui.navigation.BottomNavBar
import com.example.librai.ui.theme.PrimaryColor
import com.example.librai.viewmodel.ProfileViewModelFactory
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavController,
) {

    val context = LocalContext.current
    val firestore = remember { FirebaseFirestore.getInstance() }
    val auth = remember { FirebaseAuth.getInstance() }
    val repository = remember { BookRepository(firestore, auth) }

    val viewModel: ProfileViewModel = viewModel(
        factory = ProfileViewModelFactory(context,auth,firestore)
    )

    val name        by viewModel.displayName.collectAsState()
    val darkTheme   by viewModel.isDarkTheme.collectAsState(initial = false)
    val aiEnabled   by viewModel.aiEnabled.collectAsState()
    val avatarUrl   by viewModel.avatarUrl.collectAsState()


    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        navController.navigate("editProfile")
                    }) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit")
                    }
                    IconButton(
                        onClick = {
                            viewModel.signOut()
                            navController.navigate("auth") {
                                popUpTo(0); launchSingleTop = true
                            }
                        }
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ExitToApp, "Logout", tint = Color.White)
                    }
                },
                title = { /* empty */ },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor              = PrimaryColor,
                    navigationIconContentColor  = Color.White,
                    actionIconContentColor      = Color.White,
                    titleContentColor           = Color.White
                )
            )
        },
        bottomBar = {
            BottomNavBar(
                currentRoute = "profile",
                onItemSelected = { route ->
                    if (route != "profile") navController.navigate(route)
                }
            )
        },
        containerColor = Color.White,
        contentColor   = Color.White
    ) { inner ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(inner)
                .background(PrimaryColor)
        ) {
            Spacer(Modifier.height(24.dp))
            // Avatar placeholder
            Box(
                Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .align(Alignment.CenterHorizontally)
                    .background(Color.White.copy(alpha = .2f)),
                contentAlignment = Alignment.Center
            ) {
                val img = avatarUrl
                if (img != null) {
                    AsyncImage(
                        model = img,
                        contentDescription = "Avatar",
                        modifier = Modifier.fillMaxSize().clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(48.dp))
                }
            }

            Spacer(Modifier.height(12.dp))
            Text(name, style = MaterialTheme.typography.headlineSmall, color = Color.White, modifier  = Modifier
                .fillMaxWidth(),                  // <-- take up all the available width
                textAlign = TextAlign.Center)
            Spacer(Modifier.height(16.dp))

            Surface(
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                color = Color.White,
                modifier = Modifier.fillMaxSize()
            ) {
                Column(Modifier.padding(24.dp)) {
                    // 1) Theme toggle
                    Text("Theme", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(8.dp))
                    SegmentedToggle(
                        options = listOf("Light", "Dark"),
                        selectedIndex = if (darkTheme) 1 else 0,
                        onSelect = { idx -> viewModel.setDarkTheme(idx == 1) }
                    )

                    Spacer(Modifier.height(24.dp))

                    // 2) AI Features toggle
                    Text("AI Features", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(8.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Enable AI Recommendations", modifier = Modifier.weight(1f))
                        Switch(
                            checked = aiEnabled,
                            onCheckedChange = { viewModel.setAiEnabled(it) }
                        )
                    }

                    Spacer(Modifier.height(24.dp))

                }
            }
        }
    }
}

// A simple segmented control
@Composable
fun SegmentedToggle(
    options: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit
) {
    Row(
        Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFFE0E0E0))
            .padding(4.dp)
    ) {
        options.forEachIndexed { idx, title ->
            val isSelected = idx == selectedIndex
            Box(
                Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isSelected) PrimaryColor else Color.Transparent)
                    .clickable { onSelect(idx) }
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    title,
                    color = if (isSelected) Color.White else Color.Black.copy(alpha = .7f)
                )
            }
        }
    }
}