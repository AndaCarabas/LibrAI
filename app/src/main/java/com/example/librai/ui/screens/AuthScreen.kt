package com.example.librai.ui.screens

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.librai.R
import com.example.librai.data.repository.AuthRepository
import com.example.librai.ui.theme.AccentColor
import com.example.librai.ui.theme.PrimaryColor
import com.example.librai.viewmodel.AuthViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun SignInForm(viewModel: AuthViewModel, navController: NavController) {

//    val email by viewModel.email.collectAsState()
//    val password by viewModel.password.collectAsState()
    val context = LocalContext.current
    val signUpSuccess = viewModel.signUpSuccess
    val signUpMessage = viewModel.signUpMessage


    LaunchedEffect(signUpMessage) {
        if (signUpMessage.isNotBlank()) {
            Toast.makeText(context, signUpMessage, Toast.LENGTH_LONG).show()
        }
    }

    LaunchedEffect(signUpSuccess) {
        if (signUpSuccess) {
            navController.navigate("home") {
                popUpTo("auth") { inclusive = true } // remove auth from backstack
            }
            viewModel.resetState()
        }
    }

    Column {
        OutlinedTextField(
            value = viewModel.email,
            onValueChange = {viewModel.onEmailChange(it)},
            label = { Text("Email")},
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp)
        )
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(
            value = viewModel.password,
            onValueChange = { viewModel.onPasswordChange(it)},
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp))
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = { viewModel.signIn { navController.navigate("home") } }, modifier = Modifier
            .fillMaxWidth()
            .padding(10.dp)) {
            Text("Sign In")
        }
        TextButton(onClick = { }) {
            Text("Forgot password?", color = AccentColor)
        }

        val error by viewModel.error.collectAsState()
        error?.let {
            Text(it, color = Color.Red)
        }
    }
}

@Composable
fun SignUpForm(viewModel: AuthViewModel, navController: NavController) {

//    val name by viewModel.name.collectAsState()
//    val email by viewModel.email.collectAsState()
//    val password by viewModel.password.collectAsState()
//    val confirmPassword by viewModel.confirmPassword.collectAsState()

    val context = LocalContext.current
    val signUpSuccess = viewModel.signUpSuccess
    val signUpMessage = viewModel.signUpMessage


    LaunchedEffect(signUpMessage) {
        if (signUpMessage.isNotBlank()) {
            Toast.makeText(context, signUpMessage, Toast.LENGTH_LONG).show()
        }
    }

    LaunchedEffect(signUpSuccess) {
        if (signUpSuccess) {
            navController.navigate("home") {
                popUpTo("auth") { inclusive = true } // remove auth from backstack
            }
            viewModel.resetState()
        }
    }

    Column {
        OutlinedTextField(
            value =  viewModel.name,
            onValueChange = { viewModel.onNameChange(it) },
            label = { Text("Name") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp))
        Spacer(modifier = Modifier.height(5.dp))
        OutlinedTextField(
            value = viewModel.email,
            onValueChange = { viewModel.onEmailChange(it)},
            label = { Text("Email") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp))
        Spacer(modifier = Modifier.height(5.dp))
        OutlinedTextField(
            value = viewModel.password,
            onValueChange = {viewModel.onPasswordChange(it)},
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp))
        Spacer(modifier = Modifier.height(5.dp))
        OutlinedTextField(
            value = viewModel.confirmPassword,
            onValueChange = {viewModel.onConfirmPasswordChange(it)},
            label = { Text("Confirm Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp))
        Spacer(modifier = Modifier.height(10.dp))
        Button(onClick = { viewModel.signUp {
            navController.navigate("home")
        }}, modifier = Modifier
            .fillMaxWidth()
            .padding(10.dp)) {
            Text("Sign Up")
        }
        val error by viewModel.error.collectAsState()
        error?.let {
            Text(it, color = Color.Red)
        }
    }
}

@Composable
fun AuthScreen(navController: NavController) {
    val firebaseAuth = remember { FirebaseAuth.getInstance() }
    val viewModel = remember { AuthViewModel(AuthRepository(firebaseAuth)) }

    val pagerState = rememberPagerState(initialPage = 0, pageCount = { 2 })
    val tabs = listOf("Sign In", "Sign Up")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PrimaryColor),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Logo and App name
        Spacer(modifier = Modifier.height(60.dp))
        Image(
            painter = painterResource(id = R.drawable.icon_librai),
            contentDescription = "LibrAI Logo",
            modifier = Modifier.size(150.dp)
        )
        Spacer(modifier = Modifier.height(24.dp))

        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color.White,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                TabRow(selectedTabIndex = pagerState.currentPage, containerColor = Color.White) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = pagerState.currentPage == index,
                            onClick = {
                                CoroutineScope(Dispatchers.Main).launch {
                                    pagerState.scrollToPage(index)
                                }
                            },
                            text = {
                                Text(
                                    title,
                                    color = if (pagerState.currentPage == index) AccentColor else Color.Gray
                                )
                            }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))

                HorizontalPager(state = pagerState) { page ->
                    when (page) {
                        0 -> SignInForm(viewModel,navController)
                        1 -> SignUpForm(viewModel,navController)
                    }
                }
            }
        }
    }
}