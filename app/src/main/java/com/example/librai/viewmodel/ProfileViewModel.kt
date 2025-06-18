package com.example.librai.viewmodel

import android.content.Context
import android.net.Uri
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.preferencesDataStore
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.Preferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await


private val Context.dataStore by preferencesDataStore(name = "prefs")

class ProfileViewModel (
    context: Context,
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
) : ViewModel() {

    private val dataStore = context.dataStore
    private val storage: FirebaseStorage = FirebaseStorage.getInstance()

    private val _displayName = MutableStateFlow("Guest")
    val displayName: StateFlow<String> = _displayName

    private val _avatarUrl = MutableStateFlow<String?>(null)
    val avatarUrl: StateFlow<String?> = _avatarUrl

    private val THEME_KEY = booleanPreferencesKey("dark_theme")
    val isDarkTheme: Flow<Boolean> =
        dataStore.data.map { prefs ->
            prefs[THEME_KEY] ?: false
        }
    fun setDarkTheme(enabled: Boolean) = viewModelScope.launch {
        dataStore.edit { it[THEME_KEY] = enabled }
    }

    private val _aiEnabled = MutableStateFlow(true)
    val aiEnabled: StateFlow<Boolean> = _aiEnabled

    init {
        // load user info + settings
        auth.currentUser?.let { u ->
            _displayName.value = u.displayName ?: u.email.orEmpty().substringBefore("@")
            firestore.collection("users").document(u.uid)
                .addSnapshotListener { snap, _ ->
                    _displayName.value = snap?.getString("displayName").toString()
                }
            firestore.collection("users").document(u.uid)
                .addSnapshotListener { snap, _ ->
                    _avatarUrl.value = snap?.getString("avatarUrl")
                }
            firestore.collection("users")
                .document(u.uid)
                .collection("settings")
                .document("profile")
                .addSnapshotListener { snap, _ ->
                    snap?.getBoolean("aiEnabled")?.let { _aiEnabled.value = it }
                }
        }
    }

    fun updateProfile(
        newName: String,
        newImageUri: Uri?,
        onComplete: (Boolean, String?) -> Unit
    ) {
        val user = auth.currentUser ?: return onComplete(false, "Not signed in")
        viewModelScope.launch {
            try {
                // ① upload new image if provided
                val finalPhotoUrl = if (newImageUri != null && newImageUri.scheme in listOf("content","file")) {
                    val ref = storage
                        .getReference("avatars/${user.uid}.jpg")
                    ref.putFile(newImageUri).await()      // kotlinx-coroutines-play-services
                    ref.downloadUrl.await().toString()
                } else {
                    _avatarUrl.value  // leave as-is
                }

                // ② update FirebaseAuth profile
                val profileUpdates = UserProfileChangeRequest.Builder()
                    .setDisplayName(newName.trim())
                    .setPhotoUri(finalPhotoUrl?.let(Uri::parse))
                    .build()
                user.updateProfile(profileUpdates).await()

                // ③ write into Firestore users/{uid}
                firestore.collection("users")
                    .document(user.uid)
                    .set(mapOf(
                        "displayName" to newName.trim(),
                        "avatarUrl"   to finalPhotoUrl
                    ), SetOptions.merge())
                    .await()

                // ④ reflect locally
                _displayName.value = newName.trim()
                _avatarUrl.value   = finalPhotoUrl

                onComplete(true, null)
            } catch (e: Exception) {
                onComplete(false, e.message)
            }
        }
    }

    fun setAiEnabled(enabled: Boolean) {
        auth.currentUser?.uid?.let { uid ->
            firestore.collection("users")
                .document(uid)
                .collection("settings")
                .document("profile")
                .set(mapOf("aiEnabled" to enabled), SetOptions.merge())
        }
    }
}