package com.example.librai

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.composable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.librai.data.repository.AuthRepository
import com.example.librai.data.repository.BookRepository
import com.example.librai.ui.screens.AuthScreen
import com.example.librai.ui.screens.HomeScreen
import com.example.librai.ui.screens.LibraryScreen
import com.example.librai.ui.theme.LibrAITheme
import com.example.librai.viewmodel.AuthViewModel
import com.example.librai.viewmodel.LibraryViewModel
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)
        setContent {
            LibrAITheme {

                val navController = rememberNavController()
                val firebaseAuth = remember { FirebaseAuth.getInstance() }
                val authViewModel = remember { AuthViewModel(AuthRepository(firebaseAuth)) }
                val libraryViewModel = remember {LibraryViewModel(BookRepository(FirebaseFirestore.getInstance())) }


                NavHost(navController = navController, startDestination = "auth") {
                    composable("auth") {
                        AuthScreen(navController = navController)
                    }
                    composable("home") {
                        HomeScreen(authViewModel, navController = navController)
                    }
                    composable ("library") {
                        val userId = FirebaseAuth.getInstance().currentUser?.uid
                        if (userId != null) {
                            LibraryScreen(viewModel = libraryViewModel, userId = userId)
                        } else {
                            // Handle the case where user is null (e.g. navigate to login screen)
                            Toast.makeText(this@MainActivity,"Couldn't get uid!",Toast.LENGTH_LONG).show()

                        }
                    }

                }

            }
        }
    }
}
