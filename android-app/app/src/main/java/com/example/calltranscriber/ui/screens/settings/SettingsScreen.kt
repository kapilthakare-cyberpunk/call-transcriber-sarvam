package com.example.calltranscriber.ui.screens.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun SettingsScreen(
    sarvamApiKey: String = "",
    telegramBotToken: String = "",
    telegramChatId: String = "",
    onSaveSarvam: (String) -> Unit = {},
    onSaveTelegramToken: (String) -> Unit = {},
    onSaveTelegramChat: (String) -> Unit = {}
) {
    val sarvamState = remember(sarvamApiKey) { mutableStateOf(sarvamApiKey) }
    val tokenState = remember(telegramBotToken) { mutableStateOf(telegramBotToken) }
    val chatState = remember(telegramChatId) { mutableStateOf(telegramChatId) }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Settings")
        OutlinedTextField(
            value = sarvamState.value,
            onValueChange = { sarvamState.value = it },
            label = { Text("Sarvam API Key") }
        )
        OutlinedTextField(
            value = tokenState.value,
            onValueChange = { tokenState.value = it },
            label = { Text("Telegram Bot Token") }
        )
        OutlinedTextField(
            value = chatState.value,
            onValueChange = { chatState.value = it },
            label = { Text("Telegram Chat ID") }
        )
    }
}
