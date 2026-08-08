package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material.icons.filled.StopCircle
import androidx.compose.material.icons.filled.Warning
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
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.SosEventEntity
import com.example.services.LocationResult
import com.example.services.SmsDispatchResult
import com.example.ui.components.LiveMapCanvas
import com.example.ui.theme.EmergencyRedPrimary
import com.example.ui.theme.SafetyGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActiveSosScreen(
    sosEvent: SosEventEntity?,
    location: LocationResult?,
    dispatchResults: List<SmsDispatchResult>,
    onEndSos: () -> Unit,
    onCallNumber: (String) -> Unit,
    onManualSmsFallback: (String, String) -> Unit,
    onShareLocationLink: (String) -> Unit,
    onCopyCoordinates: (String) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("🚨 Active SOS Emergency Session") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = EmergencyRedPrimary,
                    titleContentColor = Color.White
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .testTag("active_sos_screen")
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Active Banner
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = EmergencyRedPrimary,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "EMERGENCY BROADCAST ACTIVE",
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 15.sp
                        )
                        Text(
                            text = "Session Duration: ${sosEvent?.durationSeconds ?: 0} seconds • Live GPS active",
                            color = Color(0xFFFFCDD2),
                            fontSize = 12.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Live GPS Canvas
            LiveMapCanvas(
                location = location,
                onShareLocation = onShareLocationLink,
                onCopyCoordinates = onCopyCoordinates
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Contact Notification Dispatch Status Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Notified Emergency Contacts (${dispatchResults.size})",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    if (dispatchResults.isEmpty()) {
                        Text(
                            text = "No emergency contacts were configured for automatic SMS delivery.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        dispatchResults.forEach { result ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (result.isSuccess) Icons.Default.CheckCircle else Icons.Default.Info,
                                    contentDescription = null,
                                    tint = if (result.isSuccess) SafetyGreen else MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "${result.contactName} (${result.phone})",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                    Text(
                                        text = result.statusMessage,
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                // Manual SMS button
                                IconButton(
                                    onClick = {
                                        val lat = location?.latitude ?: 37.7749
                                        val lng = location?.longitude ?: -122.4194
                                        val mapLink = "https://maps.google.com/?q=$lat,$lng"
                                        onManualSmsFallback(result.phone, "SOS ALERT! Emergency assistance requested. Location: $mapLink")
                                    }
                                ) {
                                    Icon(Icons.Default.Sms, contentDescription = "Send Manual SMS", tint = MaterialTheme.colorScheme.primary)
                                }
                                IconButton(
                                    onClick = { onCallNumber(result.phone) }
                                ) {
                                    Icon(Icons.Default.Call, contentDescription = "Call Contact", tint = SafetyGreen)
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // END SOS BUTTON
            Button(
                onClick = onEndSos,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                modifier = Modifier
                    .testTag("end_sos_session_button")
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.StopCircle, contentDescription = null, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("END SOS SESSION & SAVE TO HISTORY", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }
        }
    }
}
