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
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.librai.data.repository.BookRepository
import com.example.librai.ui.components.CameraPermissionHandler
import com.example.librai.ui.screens.AddBookOptionScreen
import com.example.librai.ui.screens.AuthScreen
import com.example.librai.ui.screens.BarcodeScannerScreen
import com.example.librai.ui.screens.BookDetailScreen
import com.example.librai.ui.screens.ProfileScreen
import com.example.librai.ui.screens.BookFormScreen
import com.example.librai.ui.screens.BookResultScreen
import com.example.librai.ui.screens.EditProfileScreen
import com.example.librai.ui.screens.HomeScreen
import com.example.librai.ui.screens.LibraryScreen
import com.example.librai.viewmodel.AuthViewModel
import com.example.librai.viewmodel.BookFormViewModel
import com.example.librai.viewmodel.BookFormViewModelFactory
import com.example.librai.viewmodel.LibraryViewModel
import com.example.librai.viewmodel.ProfileViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

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
    libraryViewModel: LibraryViewModel,
    startAt: String
) {

    val firestore = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    val bookRepository = BookRepository(firestore, auth)

    NavHost(
        navController = navController,
        startDestination = startAt
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
                LibraryScreen(navController, viewModel = libraryViewModel, userId = userId)
            } else {
                Toast.makeText(LocalContext.current, "Couldn't get uid!", Toast.LENGTH_LONG).show()
                navController.navigate("auth") {
                    popUpTo("library") { inclusive = true }
                }
            }
        }
        composable ("addBook"){
            AddBookOptionScreen(navController)
        }
        composable("scanner") {
            CameraPermissionHandler (
                rationaleMessage = "Camera permission is needed to scan barcodes.",
                onPermissionGranted = {
                    BarcodeScannerScreen { isbn ->
                        // Log.d("BookAPI", "***ISBN passed in: $isbn")
                        navController.navigate("bookForm?isbn=${isbn}") }
                }
            )
        }
        composable("bookForm?isbn={isbn}&bookId={bookId}", arguments = listOf(
            navArgument("isbn") { type = NavType.StringType; nullable = true; defaultValue = null },
            navArgument("bookId") { type = NavType.StringType; nullable = true; defaultValue = null }
        )) { backStackEntry ->
            val isbn = backStackEntry.arguments?.getString("isbn")
            val bookId = backStackEntry.arguments?.getString("bookId")

            val firestore = FirebaseFirestore.getInstance()
            val auth = FirebaseAuth.getInstance()
            val bookRepository = BookRepository(firestore, auth)

            val factory = BookFormViewModelFactory(bookRepository)
            val viewModel: BookFormViewModel = viewModel(factory = factory)

            BookFormScreen(
                isbn = isbn,
                bookId = bookId,
                navController = navController
                // If you want to use viewModel internally, you can access it via `viewModel()`
            )
        }


        composable("result/{isbn}") { backStackEntry ->
            val isbn = backStackEntry.arguments?.getString("isbn") ?: return@composable
            Log.d("BookAPI", "result ISBN passed in: $isbn")
            val viewModel: BookFormViewModel = viewModel()
            val book by viewModel.bookInfo.collectAsState()

            LaunchedEffect(isbn) {
                viewModel.loadBookInfo(isbn)
            }

            book?.let {
                BookResultScreen(it)
            } ?: Text("Loading...", modifier = Modifier.padding(16.dp))
        }

        composable(
            "bookDetail/{bookId}",
            arguments = listOf(navArgument("bookId"){ type = NavType.StringType })
        ) { backStack ->
            val bookId = backStack.arguments!!.getString("bookId")!!
            Log.d("BookAPI", "***BookID : $bookId")
            BookDetailScreen(
                bookId = backStack.arguments!!.getString("bookId")!!,
                navController = navController
            )
        }

        composable("profile") {
            ProfileScreen(navController = navController)
        }

        composable("editProfile") {
            EditProfileScreen(navController = navController)
        }
    }
}