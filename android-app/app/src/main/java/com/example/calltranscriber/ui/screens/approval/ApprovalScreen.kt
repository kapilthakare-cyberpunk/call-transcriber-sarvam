package com.example.calltranscriber.ui.screens.approval

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ApprovalScreen(
    caller: String,
    onTranscribe: () -> Unit,
    onBoth: () -> Unit,
    onSkip: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("New call: $caller")
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = onTranscribe) { Text("Transcribe") }
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = onBoth) { Text("Transcribe + Summary") }
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = onSkip) { Text("Skip") }
    }
}
