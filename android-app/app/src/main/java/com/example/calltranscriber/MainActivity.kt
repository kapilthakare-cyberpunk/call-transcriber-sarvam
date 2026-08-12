package com.example.calltranscriber

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.calltranscriber.ui.theme.CallTranscriberTheme
import com.example.calltranscriber.util.checkRequiredPermissions

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CallTranscriberTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    val navController = rememberNavController()
                    NavHost(navController = navController, startDestination = "home") {
                        composable("home") {
                            if (!checkRequiredPermissions(this@MainActivity)) {
                                PermissionRequestScreen(onGranted = {
                                    navController.navigate("home") {
                                        popUpTo("home") { inclusive = true }
                                    }
                                })
                            } else {
                                HomeScreen()
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PermissionRequestScreen(onGranted: () -> Unit) {
    val permissions = mutableListOf(
        Manifest.permission.READ_MEDIA_AUDIO,
        Manifest.permission.POST_NOTIFICATIONS,
        Manifest.permission.FOREGROUND_SERVICE,
        Manifest.permission.READ_PHONE_STATE
    )

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Permissions required")
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = {
            ActivityCompat.requestPermissions(
                /* todo: */
                /* placeholder for activity reference */,
                permissions.toTypedArray(),
                1001
            )
            onGranted()
        }) {
            Text("Grant permissions")
        }
    }
}

@Composable
fun HomeScreen() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Call Transcriber")
        Spacer(modifier = Modifier.height(16.dp))
        Text("Event-driven call detection enabled.")
    }
}
