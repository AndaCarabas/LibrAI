package com.example.librai

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.composable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.librai.data.repository.AuthRepository
import com.example.librai.ui.screens.AuthScreen
import com.example.librai.ui.screens.HomeScreen
import com.example.librai.ui.theme.LibrAITheme
import com.example.librai.viewmodel.AuthViewModel
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)
        setContent {
            LibrAITheme {

                val navController = rememberNavController()
                val firebaseAuth = remember { FirebaseAuth.getInstance() }
                val viewModel = remember { AuthViewModel(AuthRepository(firebaseAuth)) }

                NavHost(navController = navController, startDestination = "auth") {
                    composable("auth") {
                        AuthScreen(navController = navController)
                    }
                    composable("home") {
                        HomeScreen(viewModel, navController = navController)
                    }

                }

            }
        }
    }
}

//@Composable
//fun SignInForm() {
//
//    var email by remember { mutableStateOf("") }
//    var password by remember { mutableStateOf("") }
//
//    Column {
//        OutlinedTextField(
//            value = email,
//            onValueChange = { email = it },
//            label = { Text("Email")},
//            modifier = Modifier.fillMaxWidth().padding(10.dp)
//        )
//        Spacer(modifier = Modifier.height(12.dp))
//        OutlinedTextField(
//            value = password,
//            onValueChange = { password = it },
//            label = { Text("Password") },
//            visualTransformation = PasswordVisualTransformation(),
//            modifier = Modifier.fillMaxWidth().padding(10.dp))
//        Spacer(modifier = Modifier.height(16.dp))
//        Button(onClick = { }, modifier = Modifier.fillMaxWidth().padding(10.dp)) {
//            Text("Sign In")
//        }
//        TextButton(onClick = { }) {
//            Text("Forgot password?", color = AccentColor)
//        }
//    }
//}

//@Composable
//fun SignUpForm() {
//
//    var name by remember { mutableStateOf("") }
//    var email by remember { mutableStateOf("") }
//    var password by remember { mutableStateOf("") }
//    var confirmPassword by remember { mutableStateOf("") }
//
//    Column {
//        OutlinedTextField(
//            value = name,
//            onValueChange = {name = it},
//            label = { Text("Name") },
//            modifier = Modifier.fillMaxWidth().padding(10.dp))
//        Spacer(modifier = Modifier.height(5.dp))
//        OutlinedTextField(
//            value = email,
//            onValueChange = {email = it},
//            label = { Text("Email") },
//            modifier = Modifier.fillMaxWidth().padding(10.dp))
//        Spacer(modifier = Modifier.height(5.dp))
//        OutlinedTextField(
//            value = password,
//            onValueChange = {password = it},
//            label = { Text("Password") },
//            visualTransformation = PasswordVisualTransformation(),
//            modifier = Modifier.fillMaxWidth().padding(10.dp))
//        Spacer(modifier = Modifier.height(5.dp))
//        OutlinedTextField(
//            value = confirmPassword,
//            onValueChange = {confirmPassword = it},
//            label = { Text("Confirm Password") },
//            visualTransformation = PasswordVisualTransformation(),
//            modifier = Modifier.fillMaxWidth().padding(10.dp))
//        Spacer(modifier = Modifier.height(10.dp))
//        Button(onClick = { }, modifier = Modifier.fillMaxWidth().padding(10.dp)) {
//            Text("Sign Up")
//        }
//    }
//}

//@Composable
//fun AuthScreenWithPager(viewModel: AuthViewModel = hiltViewModel()) {
//    val pagerState = rememberPagerState(initialPage = 0, pageCount = { 2 })
//    val tabs = listOf("Sign In", "Sign Up")
//
//
//    Column(
//        modifier = Modifier
//            .fillMaxSize()
//            .background(PrimaryColor),
//        horizontalAlignment = Alignment.CenterHorizontally
//    ) {
//        Spacer(modifier = Modifier.height(80.dp))
//
//        Image(
//            painter = painterResource(id = R.drawable.icon_librai),
//            contentDescription = "LibrAI Logo",
//            modifier = Modifier.size(150.dp)
//        )
//
//       // Text("LibrAI", style = MaterialTheme.typography.headlineMedium, color = Color.White)
//
//        Spacer(modifier = Modifier.height(40.dp))
//
//        Surface(
//            shape = RoundedCornerShape(24.dp),
//            color = Color.White,
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(16.dp)
//        ) {
//            Column(modifier = Modifier.padding(16.dp)) {
//                TabRow(selectedTabIndex = pagerState.currentPage, containerColor = Color.White) {
//                    tabs.forEachIndexed { index, title ->
//                        Tab(
//                            selected = pagerState.currentPage == index,
//                            onClick = {
//                                CoroutineScope(Dispatchers.Main).launch {
//                                    pagerState.scrollToPage(index)
//                                }
//                            },
//                            text = {
//                                Text(
//                                    title,
//                                    color = if (pagerState.currentPage == index) AccentColor else Color.Gray
//                                )
//                            }
//                        )
//                    }
//                }
//
//                Spacer(modifier = Modifier.height(16.dp))
//
//                HorizontalPager(state = pagerState) { page ->
//                    when (page) {
//                        0 -> SignInForm()
//                        1 -> SignUpForm()
//                    }
//                }
//            }
//        }
//    }
//}
//
//@Composable
//fun SplashScreen(onSplashFinished: () -> Unit) {
//    LaunchedEffect(Unit) {
//        delay(2000L) // Splash duration (2 seconds)
//        onSplashFinished()
//    }
//
//    Box(
//        modifier = Modifier
//            .fillMaxSize()
//            .background(PrimaryColor),
//        contentAlignment = Alignment.Center
//    ) {
//        Column(horizontalAlignment = Alignment.CenterHorizontally) {
//            Image(
//                painter = painterResource(id = R.drawable.icon_librai),
//                contentDescription = null,
//                modifier = Modifier.size(150.dp)
//            )
//            //Icon(Icons.Default.MenuBook, contentDescription = null, tint = Color.White, modifier = Modifier.size(64.dp))
//            Spacer(modifier = Modifier.height(16.dp))
//            Text("LibrAI", color = Color.White, style = MaterialTheme.typography.headlineMedium)
//        }
//    }
//}