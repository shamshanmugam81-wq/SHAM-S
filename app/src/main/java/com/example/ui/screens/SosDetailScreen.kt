package com.example.ui.screens

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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Share
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
import com.example.ui.components.LiveMapCanvas
import com.example.ui.theme.EmergencyRedPrimary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SosDetailScreen(
    sosEvent: SosEventEntity?,
    onBack: () -> Unit,
    onDeleteEvent: (Long) -> Unit,
    onShareLink: (String) -> Unit
) {
    val event = sosEvent ?: return

    val dateStr = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date(event.timestamp))
    val endedStr = event.endedAt?.let { SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(it)) } ?: "Ongoing / N/A"

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("SOS Event #${event.id} Details") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { onDeleteEvent(event.id); onBack() }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete Event", tint = EmergencyRedPrimary)
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .testTag("sos_detail_screen")
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "Event Summary", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(10.dp))

                    Text(text = "Status: ${event.status}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Text(text = "Triggered At: $dateStr", fontSize = 13.sp)
                    Text(text = "Ended At: $endedStr", fontSize = 13.sp)
                    Text(text = "Duration: ${event.durationSeconds} seconds", fontSize = 13.sp)
                    Text(text = "Contacts Notified: ${event.contactsNotifiedCount}", fontSize = 13.sp)

                    Spacer(modifier = Modifier.height(10.dp))

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Text(
                            text = event.alertSummary.ifBlank { "SOS alert broadcast to configured contacts." },
                            fontSize = 12.sp,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            LiveMapCanvas(
                location = LocationResult(
                    latitude = event.latitude,
                    longitude = event.longitude,
                    addressEstimate = event.addressLocation
                ),
                onShareLocation = onShareLink,
                onCopyCoordinates = {}
            )

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = { onShareLink(event.mapLink) },
                modifier = Modifier
                    .testTag("share_event_link_button")
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("SHARE EVENT MAP LINK", fontWeight = FontWeight.Bold)
            }
        }
    }
}
