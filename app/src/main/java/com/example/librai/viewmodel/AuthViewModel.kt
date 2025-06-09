package com.example.librai.viewmodel

import android.widget.Toast
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.librai.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.google.firebase.auth.FirebaseAuth


class AuthViewModel (private val repository: AuthRepository) : ViewModel() {

//    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
//    val authState = _authState.asStateFlow()

    var signUpSuccess by mutableStateOf(false)
        private set

    var signUpMessage by mutableStateOf("")
        private set

    private val _userName = mutableStateOf<String?>(null)
    val userName: State<String?> = _userName

    private val _userUid = mutableStateOf<String?>(null)
    val userUid: State<String?> = _userUid

    var email by mutableStateOf("")
    var password by mutableStateOf("")
    var confirmPassword by mutableStateOf("")
    var name by mutableStateOf("")

    // Error message to show validation or Firebase errors
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error


    fun onEmailChange(newEmail: String) {
        email = newEmail
    }

    fun onPasswordChange(newPassword: String) {
        password = newPassword
    }

    fun onNameChange(newName: String) {
        name = newName
    }

    fun onConfirmPasswordChange(newConfirmPassword: String) {
        confirmPassword = newConfirmPassword
    }

    fun fetchUserName() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        viewModelScope.launch {
            val result = repository.getUserData(uid)
            if (result.isSuccess) {
                _userName.value = result.getOrNull()?.name
            }
        }
    }

    fun fetchUID() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        viewModelScope.launch {
            val result = repository.getUserData(uid)
            if (result.isSuccess) {
                _userUid.value = result.getOrNull()?.id
            }
        }
    }

    fun signUp(onSuccess: () -> Unit) {

        if (name.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            _error.value = "All fields are required"
            return
        }

        if (password != confirmPassword) {
            _error.value = "Passwords don't match!"
            return
        }

        viewModelScope.launch {
            //_authState.value = AuthState.Loading
            val result = repository.signUp(email, password, name)
            if (result.isSuccess) {
                signUpMessage = "Sign Up successful. Hello, $name!"
                signUpSuccess = true

                //onSuccess()
               // _authState.value = AuthState.Success
            } else {
                signUpMessage = result.exceptionOrNull()?.message?: "Unknown error"
                signUpSuccess = false
                _error.value = result.exceptionOrNull()?.message
                //_authState.value = AuthState.Error(result.exceptionOrNull()?.message?: "Unknown error")
            }
        }
    }

    fun resetState() {
        signUpMessage = ""
        signUpSuccess = false
    }

    fun signIn(onSuccess: () -> Unit) {

        if (email.isEmpty() || password.isEmpty()) {
            _error.value = "Email and password must not be empty"
            return
        }

        viewModelScope.launch {
            val result = repository.signIn(email, password)
            if (result.isSuccess)
            {
                signUpMessage = "Sign In successful!"
                signUpSuccess = true
                //onSuccess()
                //_authState.value = AuthState.Success
            } else {
                signUpMessage = result.exceptionOrNull()?.message?: "Unknown error"
                signUpSuccess = false
                _error.value = result.exceptionOrNull()?.message
                //_authState.value = AuthState.Error(result.exceptionOrNull()?.message?: "Unknown error")
            }
        }
    }

    fun logout() {
        repository.logoutUser()
        _userName.value = null
        //_authState.value = AuthState.Idle
    }
}



sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    object Success : AuthState()
    data class Error(val message: String) : AuthState()
}