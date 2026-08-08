package com.example.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.AppSettings
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    appSettings: AppSettings,
    onBack: () -> Unit,
    onSaveSettings: (AppSettings) -> Unit
) {
    var countdownDuration by remember { mutableStateOf(appSettings.countdownDurationSeconds.toString()) }
    var autoCall by remember { mutableStateOf(appSettings.autoCallEmergencyService) }
    var emergencyNumber by remember { mutableStateOf(appSettings.emergencyNumberToCall) }
    var customMsg by remember { mutableStateOf(appSettings.customMessageTemplate) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("App Settings & Configuration") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .testTag("settings_screen")
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "SOS Trigger Settings", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = countdownDuration,
                        onValueChange = { countdownDuration = it },
                        label = { Text("SOS Countdown Duration (Seconds)") },
                        leadingIcon = { Icon(Icons.Default.Timer, contentDescription = null) },
                        modifier = Modifier
                            .testTag("settings_countdown_input")
                            .fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Call, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Auto Call Emergency Number", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("Initiate phone call automatically on SOS activation", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = autoCall,
                            onCheckedChange = { autoCall = it },
                            modifier = Modifier.testTag("settings_autocall_switch")
                        )
                    }

                    if (autoCall) {
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = emergencyNumber,
                            onValueChange = { emergencyNumber = it },
                            label = { Text("Emergency Number (e.g., 112)") },
                            modifier = Modifier
                                .testTag("settings_emergency_num_input")
                                .fillMaxWidth(),
                            singleLine = true
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = customMsg,
                        onValueChange = { customMsg = it },
                        label = { Text("Custom Emergency Message Prefix (Optional)") },
                        leadingIcon = { Icon(Icons.Default.Message, contentDescription = null) },
                        modifier = Modifier
                            .testTag("settings_custom_msg_input")
                            .fillMaxWidth(),
                        minLines = 2
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = {
                            val durationSeconds = countdownDuration.toIntOrNull() ?: 5
                            onSaveSettings(
                                AppSettings(
                                    countdownDurationSeconds = durationSeconds,
                                    autoCallEmergencyService = autoCall,
                                    emergencyNumberToCall = emergencyNumber,
                                    customMessageTemplate = customMsg
                                )
                            )
                            scope.launch { snackbarHostState.showSnackbar("Settings Saved Successfully") }
                        },
                        modifier = Modifier
                            .testTag("save_settings_button")
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("SAVE CONFIGURATION", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
