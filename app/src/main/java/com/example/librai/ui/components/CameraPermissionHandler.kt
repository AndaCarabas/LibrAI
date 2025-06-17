@file:OptIn(ExperimentalPermissionsApi::class)

package com.example.librai.ui.components

import android.Manifest
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.accompanist.permissions.*

@Composable
fun CameraPermissionHandler(
    rationaleMessage: String = "Camera permission is required to use this feature.",
    permanentDenialMessage: String = "Camera permission is permanently denied. Please enable it in settings.",
    onPermissionGranted: @Composable () -> Unit
) {
    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)

    LaunchedEffect(Unit) {
        cameraPermissionState.launchPermissionRequest()
    }

//    when (cameraPermissionState.status) {
//        is PermissionStatus.Granted -> {
//            onPermissionGranted()
//        }
//
//        is PermissionStatus.Denied -> {
//            val status = cameraPermissionState.status as PermissionStatus.Denied
//
//            if (status.shouldShowRationale) {
//                Column {
//                    Text("Camera permission is needed to scan barcodes.")
//                    Button(onClick = { cameraPermissionState.launchPermissionRequest() }) {
//                        Text("Grant Permission")
//                    }
//                }
//            } else {
//                Column {
//                    Text("Camera permission is permanently denied. Please enable it in settings.")
//                    Button(onClick = { cameraPermissionState.launchPermissionRequest() }) {
//                        Text("Try Again")
//                    }
//                }
//            }
//        }
//    }

    when (cameraPermissionState.status) {
        is PermissionStatus.Granted -> {
            onPermissionGranted()
        }

        is PermissionStatus.Denied -> {
            val status = cameraPermissionState.status as PermissionStatus.Denied
            val message = if (status.shouldShowRationale) rationaleMessage else permanentDenialMessage

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = message, style = MaterialTheme.typography.bodyLarge)
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = { cameraPermissionState.launchPermissionRequest() }) {
                    Text("Grant Permission")
                }
            }
        }
    }
}

