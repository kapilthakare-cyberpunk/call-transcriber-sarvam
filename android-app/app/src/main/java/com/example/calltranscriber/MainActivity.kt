package com.example.calltranscriber

import android.Manifest
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.calltranscriber.ui.theme.CallTranscriberTheme
import com.example.calltranscriber.ui.screens.MainNavHost

class MainActivity : ComponentActivity() {

    private val requiredPermissions = if (android.os.Build.VERSION.SDK_INT >= 33) {
        arrayOf(
            Manifest.permission.READ_MEDIA_AUDIO,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.POST_NOTIFICATIONS,
            Manifest.permission.FOREGROUND_SERVICE
        )
    } else {
        arrayOf(
            Manifest.permission.READ_MEDIA_AUDIO,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.FOREGROUND_SERVICE
        )
    }

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { granted ->
        val allGranted = granted.all { it.value }
        if (allGranted) {
            startService(Intent(this, CallTranscriberForegroundService::class.java))
        }
        setContent {
            CallTranscriberTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    if (hasAllPermissions()) {
                        MainNavHost()
                    } else {
                        PermissionRequestScreen(onGranted = {
                            permissionLauncher.launch(requiredPermissions)
                        })
                    }
                }
            }
        }
    }

    private fun hasAllPermissions(): Boolean =
        requiredPermissions.all { permission ->
            ContextCompat.checkSelfPermission(this, permission) == android.content.pm.PackageManager.PERMISSION_GRANTED
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (hasAllPermissions()) {
            startService(Intent(this, CallTranscriberForegroundService::class.java))
        }
        setContent {
            CallTranscriberTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    if (hasAllPermissions()) {
                        MainNavHost()
                    } else {
                        PermissionRequestScreen(onGranted = {
                            permissionLauncher.launch(requiredPermissions)
                        })
                    }
                }
            }
        }
    }
}

@Composable
fun PermissionRequestScreen(onGranted: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Permissions required", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onGranted) {
            Text(text = "Grant permissions")
        }
    }
}
