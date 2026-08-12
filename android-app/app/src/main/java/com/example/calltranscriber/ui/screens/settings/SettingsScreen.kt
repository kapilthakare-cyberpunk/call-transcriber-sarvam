package com.example.calltranscriber.ui.screens.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun SettingsScreen() {
    var sarvamKey by remember { mutableStateOf("") }
    var provider by remember { mutableStateOf("local") }
    var telegramToken by remember { mutableStateOf("") }
    var telegramChat by remember { mutableStateOf("") }
    var workKeywords by remember { mutableStateOf("") }
    var personalKeywords by remember { mutableStateOf("") }
    var llmModel by remember { mutableStateOf("qwen2.5:1.5b") }
    var expanded by remember { mutableStateOf(false) }

    val providers = listOf("local", "openrouter", "groq", "cerebras", "mistral", "ollama", "opencode")
    val models = listOf("qwen2.5:1.5b", "llama3.1:8b", "mistral:7b", "gemma2:9b")

    Surface(color = MaterialTheme.colorScheme.background) {
        LazyColumn(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            item {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
                    Text(text = "Settings", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.onBackground)
                    Text(text = "Keys, providers, and classification rules", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            item {
                ElevatedCard(modifier = Modifier.padding(horizontal = 16.dp), colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(text = "Transcription", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                        OutlinedTextField(value = sarvamKey, onValueChange = { sarvamKey = it }, modifier = Modifier.fillMaxWidth(), singleLine = true, placeholder = { Text("Sarvam API key") })
                    }
                }
            }

            item {
                ElevatedCard(modifier = Modifier.padding(horizontal = 16.dp), colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(text = "Summary provider", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                        ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
                            OutlinedTextField(
                                value = provider,
                                onValueChange = {},
                                modifier = Modifier.fillMaxWidth().menuAnchor(),
                                readOnly = true,
                                trailingIcon = { Icon(Icons.Default.ArrowDropDown, contentDescription = null) },
                                label = { Text("Provider") }
                            )
                            ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                                providers.forEach { p ->
                                    DropdownMenuItem(text = { Text(p) }, onClick = { provider = p; expanded = false })
                                }
                            }
                        }
                        OutlinedTextField(value = llmModel, onValueChange = { llmModel = it }, modifier = Modifier.fillMaxWidth(), singleLine = true, placeholder = { Text("Model id, e.g. qwen2.5:1.5b") })
                    }
                }
            }

            item {
                ElevatedCard(modifier = Modifier.padding(horizontal = 16.dp), colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(text = "Telegram", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                        OutlinedTextField(value = telegramToken, onValueChange = { telegramToken = it }, modifier = Modifier.fillMaxWidth(), singleLine = true, placeholder = { Text("Bot token") })
                        OutlinedTextField(value = telegramChat, onValueChange = { telegramChat = it }, modifier = Modifier.fillMaxWidth(), singleLine = true, placeholder = { Text("Chat id") })
                    }
                }
            }

            item {
                ElevatedCard(modifier = Modifier.padding(horizontal = 16.dp), colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(text = "Classification", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                        OutlinedTextField(value = workKeywords, onValueChange = { workKeywords = it }, modifier = Modifier.fillMaxWidth(), singleLine = true, placeholder = { Text("Work keywords, comma separated") })
                        OutlinedTextField(value = personalKeywords, onValueChange = { personalKeywords = it }, modifier = Modifier.fillMaxWidth(), singleLine = true, placeholder = { Text("Personal keywords, comma separated") })
                    }
                }
            }

            item {
                Column(modifier = Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(onClick = {}, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer, contentColor = MaterialTheme.colorScheme.onPrimaryContainer)) {
                        Text("Save settings")
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}
