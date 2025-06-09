@file:OptIn(ExperimentalPermissionsApi::class)

package com.example.librai.ui.components

import android.Manifest
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import com.google.accompanist.permissions.*

@Composable
fun CameraPermissionHandler(onPermissionGranted: @Composable () -> Unit) {
    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)

    LaunchedEffect(Unit) {
        cameraPermissionState.launchPermissionRequest()
    }

    when (cameraPermissionState.status) {
        is PermissionStatus.Granted -> {
            onPermissionGranted()
        }

        is PermissionStatus.Denied -> {
            val status = cameraPermissionState.status as PermissionStatus.Denied

            if (status.shouldShowRationale) {
                Column {
                    Text("Camera permission is needed to scan barcodes.")
                    Button(onClick = { cameraPermissionState.launchPermissionRequest() }) {
                        Text("Grant Permission")
                    }
                }
            } else {
                Column {
                    Text("Camera permission is permanently denied. Please enable it in settings.")
                    Button(onClick = { cameraPermissionState.launchPermissionRequest() }) {
                        Text("Try Again")
                    }
                }
            }
        }
    }
}

