package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.EmergencyContactEntity
import com.example.data.local.UserEntity
import com.example.services.LocationResult
import com.example.ui.components.SosButton
import com.example.ui.components.StatusBanner
import com.example.ui.theme.EmergencyRedPrimary
import com.example.ui.theme.SafetyGreen
import com.example.ui.theme.WarningAmber

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    user: UserEntity?,
    contacts: List<EmergencyContactEntity>,
    location: LocationResult?,
    isSosActive: Boolean,
    isPanicAlarmRunning: Boolean,
    isFlashlightOn: Boolean,
    onTriggerSos: () -> Unit,
    onNavigateToActiveSos: () -> Unit,
    onTogglePanicAlarm: () -> Unit,
    onToggleFlashlight: () -> Unit,
    onInitiateCall: (String) -> Unit,
    onNavigateContacts: () -> Unit,
    onNavigateMap: () -> Unit,
    onNavigateHistory: () -> Unit,
    onNavigateProfile: () -> Unit,
    onNavigateSettings: () -> Unit,
    onNavigatePermissions: () -> Unit,
    onNavigateHelp: () -> Unit
) {
    var showCallConfirmDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Smart SOS", fontWeight = FontWeight.Black, fontSize = 20.sp)
                        Text(
                            text = if (user != null) "User: ${user.name}" else "Emergency Safety System",
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White,
                    actionIconContentColor = Color.White
                ),
                actions = {
                    IconButton(onClick = onNavigateProfile, modifier = Modifier.testTag("nav_profile_button")) {
                        Icon(Icons.Default.Person, contentDescription = "Profile")
                    }
                    IconButton(onClick = onNavigateSettings, modifier = Modifier.testTag("nav_settings_button")) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .testTag("dashboard_screen")
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Status Banner
            StatusBanner(
                location = location,
                contactCount = contacts.size,
                isSosActive = isSosActive
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Main SOS Trigger Circle
            Text(
                text = "EMERGENCY ASSISTANCE",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (isSosActive) {
                Button(
                    onClick = onNavigateToActiveSos,
                    colors = ButtonDefaults.buttonColors(containerColor = EmergencyRedPrimary),
                    modifier = Modifier
                        .testTag("view_active_sos_session_button")
                        .fillMaxWidth()
                        .height(56.dp)
                ) {
                    Icon(Icons.Default.Call, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("VIEW ACTIVE SOS EMERGENCY SESSION", fontWeight = FontWeight.Bold)
                }
            } else {
                SosButton(
                    onClick = onTriggerSos
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Emergency Call & Panic Alarm Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Instant Safety Actions",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Quick Emergency Call 112
                        Button(
                            onClick = { showCallConfirmDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = SafetyGreen),
                            modifier = Modifier
                                .testTag("call_112_button")
                                .weight(1f)
                                .height(48.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("CALL 112", fontWeight = FontWeight.Bold)
                        }

                        // Panic Alarm Siren Toggle
                        Button(
                            onClick = onTogglePanicAlarm,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isPanicAlarmRunning) WarningAmber else MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = if (isPanicAlarmRunning) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            modifier = Modifier
                                .testTag("toggle_panic_alarm_button")
                                .weight(1f)
                                .height(48.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.VolumeUp, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isPanicAlarmRunning) "SIREN ON" else "PANIC SIREN",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Strobe Flashlight Toggle
                    Button(
                        onClick = onToggleFlashlight,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isFlashlightOn) Color(0xFF455A64) else MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = if (isFlashlightOn) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier
                            .testTag("toggle_flashlight_button")
                            .fillMaxWidth()
                            .height(44.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.FlashlightOn, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isFlashlightOn) "FLASHLIGHT LIGHTING ON" else "TOGGLE STROBE FLASHLIGHT",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Navigation Grid Options
            Text(
                text = "Emergency Features",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.Start)
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                FeatureShortcutCard(
                    title = "Contacts",
                    subtitle = "${contacts.size} Configured",
                    icon = Icons.Default.Contacts,
                    modifier = Modifier
                        .testTag("nav_contacts_card")
                        .weight(1f),
                    onClick = onNavigateContacts
                )
                FeatureShortcutCard(
                    title = "Live Map",
                    subtitle = "GPS Location",
                    icon = Icons.Default.Map,
                    modifier = Modifier
                        .testTag("nav_map_card")
                        .weight(1f),
                    onClick = onNavigateMap
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                FeatureShortcutCard(
                    title = "SOS History",
                    subtitle = "Past Alerts",
                    icon = Icons.Default.History,
                    modifier = Modifier
                        .testTag("nav_history_card")
                        .weight(1f),
                    onClick = onNavigateHistory
                )
                FeatureShortcutCard(
                    title = "Safety Info",
                    subtitle = "Permissions",
                    icon = Icons.Default.Security,
                    modifier = Modifier
                        .testTag("nav_safety_card")
                        .weight(1f),
                    onClick = onNavigatePermissions
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            FeatureShortcutCard(
                title = "Emergency Numbers & Helpline Guide",
                subtitle = "India 112, Police, Women Helpline, Ambulance",
                icon = Icons.Default.Info,
                modifier = Modifier
                    .testTag("nav_help_card")
                    .fillMaxWidth(),
                onClick = onNavigateHelp
            )
        }
    }

    // Call 112 Confirmation Dialog
    if (showCallConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showCallConfirmDialog = false },
            title = { Text("Call Official Emergency Services (112)?", fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to dial official emergency number 112 now?") },
            confirmButton = {
                Button(
                    onClick = {
                        showCallConfirmDialog = false
                        onInitiateCall("112")
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmergencyRedPrimary)
                ) {
                    Text("CALL 112 NOW")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCallConfirmDialog = false }) {
                    Text("CANCEL")
                }
            }
        )
    }
}

@Composable
private fun FeatureShortcutCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(38.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(text = title, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Text(text = subtitle, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
