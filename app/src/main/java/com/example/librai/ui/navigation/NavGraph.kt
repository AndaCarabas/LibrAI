package com.example.librai.ui.navigation

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.librai.ui.components.CameraPermissionHandler
import com.example.librai.ui.screens.AuthScreen
import com.example.librai.ui.screens.BarcodeScannerScreen
import com.example.librai.ui.screens.BookResultScreen
import com.example.librai.ui.screens.HomeScreen
import com.example.librai.ui.screens.LibraryScreen
import com.example.librai.viewmodel.AuthViewModel
import com.example.librai.viewmodel.BookViewModel
import com.example.librai.viewmodel.LibraryViewModel
import com.google.firebase.auth.FirebaseAuth

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object SignIn : Screen("signin")
    object SignUp : Screen("signup")
    object Home : Screen("home")
    object BookDetails : Screen("bookdetails/{bookId}")
}

@Composable
fun AppNavGraph(
    navController: NavHostController,
    authViewModel: AuthViewModel,
    libraryViewModel: LibraryViewModel
) {
    NavHost(
        navController = navController,
        startDestination = "auth"
    ) {
        composable("auth") {
            AuthScreen(navController = navController)
        }
        composable("home") {
            HomeScreen(authViewModel, navController)
        }
        composable("library") {
            val userId = FirebaseAuth.getInstance().currentUser?.uid
            if (userId != null) {
                LibraryScreen(viewModel = libraryViewModel, userId = userId)
            } else {
                Toast.makeText(LocalContext.current, "Couldn't get uid!", Toast.LENGTH_LONG).show()
                navController.navigate("auth") {
                    popUpTo("library") { inclusive = true }
                }
            }
        }
        composable("scanner") {
            CameraPermissionHandler {
                BarcodeScannerScreen { isbn ->
                    Log.d("BookAPI", "***ISBN passed in: $isbn")
                    navController.navigate("result/$isbn") }
            }

        }
        composable("result/{isbn}") { backStackEntry ->
            val isbn = backStackEntry.arguments?.getString("isbn") ?: return@composable
            Log.d("BookAPI", "result ISBN passed in: $isbn")
            val viewModel: BookViewModel = viewModel()
            val book by viewModel.bookInfo.collectAsState()

            LaunchedEffect(isbn) {
                viewModel.loadBookInfo(isbn)
            }

            book?.let {
                BookResultScreen(it)
            } ?: Text("Loading...", modifier = Modifier.padding(16.dp))
        }
    }
}