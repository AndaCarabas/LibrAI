package com.example.librai.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CenterFocusStrong
import androidx.compose.material3.Button
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.librai.ui.navigation.BottomNavBar
import com.example.librai.ui.theme.HighlightColor
import com.example.librai.viewmodel.AuthViewModel

@Composable
fun HomeScreen(viewModel: AuthViewModel, navController: NavController) {
    val userName by viewModel.userName
    val userID by viewModel.userUid
    var showSheet by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.fetchUserName()
    }

    LaunchedEffect(Unit) {
        viewModel.fetchUID()
    }

    Scaffold(
        containerColor = Color.White,
        bottomBar = {
            BottomNavBar(
                currentRoute = "home",
                onItemSelected = { route ->
                    if (route != "home") navController.navigate(route)
                }
            )
        }
    ){ innerPadding ->
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Hello, $userName!", style = MaterialTheme.typography.headlineMedium)
                Spacer(modifier = Modifier.height(24.dp))
                Button(onClick = {
                    // optional: call signOut logic
                    viewModel.logout()
                    navController.navigate("auth") { popUpTo(0) }
                }) {
                    Text("Log Out")
                }
                Spacer(modifier = Modifier.height(24.dp))
                Button(onClick = {
                    navController.navigate("library")
                }) {
                    Text("My Library")
                }
                Spacer(modifier = Modifier.height(24.dp))
                FloatingActionButton(
                    onClick = { showSheet = true },
                    modifier = Modifier
                        .align(Alignment.End)
                        .padding(16.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Book")
                }

                if (showSheet) {
                    AddBookOptionBottomSheet(
                        onScan = { navController.navigate("scanner") },
                        onManual = { navController.navigate("bookForm") },
                        onDismiss = { showSheet = false }
                    )
                }
//        Button(onClick = {
//            navController.navigate("addBook") {
//            }
//        }) {
//            Text("Add Book")
//        }
//        Spacer(modifier = Modifier.height(24.dp))
//        Button(onClick = {
//            navController.navigate("bookForm?isbn=${scannedIsbn}"){
//            }
//        }) {
//            Text("Scanner")
//        }
            }
        }
    }


}