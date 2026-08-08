package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.services.LocationResult
import com.example.ui.theme.EmergencyRedPrimary
import com.example.ui.theme.SafetyGreen

@Composable
fun LiveMapCanvas(
    modifier: Modifier = Modifier,
    location: LocationResult?,
    onShareLocation: (String) -> Unit,
    onCopyCoordinates: (String) -> Unit
) {
    val lat = location?.latitude ?: 37.7749
    val lng = location?.longitude ?: -122.4194
    val mapLink = "https://maps.google.com/?q=$lat,$lng"
    val coordText = "Lat: %.5f, Lng: %.5f".format(lat, lng)

    val transition = rememberInfiniteTransition(label = "radar_sweep")
    val sweepRadius by transition.animateFloat(
        initialValue = 0f,
        targetValue = 280f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "radar_radius"
    )

    Card(
        modifier = modifier
            .testTag("live_map_canvas")
            .fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1E1E24)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Map Visual Viewport
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF12131C))
                    .border(1.dp, Color(0xFF333646), RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val center = Offset(size.width / 2f, size.height / 2f)

                    // Grid lines
                    val step = 40.dp.toPx()
                    var x = 0f
                    while (x < size.width) {
                        drawLine(
                            color = Color(0xFF232738),
                            start = Offset(x, 0f),
                            end = Offset(x, size.height),
                            strokeWidth = 1f,
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f))
                        )
                        x += step
                    }
                    var y = 0f
                    while (y < size.height) {
                        drawLine(
                            color = Color(0xFF232738),
                            start = Offset(0f, y),
                            end = Offset(size.width, y),
                            strokeWidth = 1f,
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f))
                        )
                        y += step
                    }

                    // Concentric Radar Circles
                    drawCircle(color = Color(0xFF2A314A), radius = 60.dp.toPx(), center = center, style = androidx.compose.ui.graphics.drawscope.Stroke(1.5f))
                    drawCircle(color = Color(0xFF2A314A), radius = 120.dp.toPx(), center = center, style = androidx.compose.ui.graphics.drawscope.Stroke(1.5f))

                    // Pulse Circle
                    drawCircle(
                        color = Color(0x33FF5252),
                        radius = sweepRadius,
                        center = center
                    )

                    // Target Pin Marker
                    drawCircle(color = EmergencyRedPrimary, radius = 14.dp.toPx(), center = center)
                    drawCircle(color = Color.White, radius = 6.dp.toPx(), center = center)
                }

                // Live GPS Badge
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xCC000000),
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.GpsFixed,
                            contentDescription = "GPS Active",
                            tint = SafetyGreen,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (location?.isFallback == true) "GPS Cached/Estimated" else "LIVE GPS LOCK",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Coordinate Readout & Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Current Coordinates",
                        fontSize = 12.sp,
                        color = Color(0xFFB0B3C6),
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = coordText,
                        fontSize = 14.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = location?.addressEstimate ?: "Estimating address...",
                        fontSize = 11.sp,
                        color = Color(0xFF8B8EA2)
                    )
                }

                IconButton(
                    onClick = { onCopyCoordinates(coordText) },
                    modifier = Modifier.testTag("copy_coordinates_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copy Coordinates",
                        tint = Color.White
                    )
                }

                IconButton(
                    onClick = { onShareLocation(mapLink) },
                    modifier = Modifier.testTag("share_location_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Share Location Link",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}
