package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.services.LocationResult

@Composable
fun StatusBanner(
    modifier: Modifier = Modifier,
    location: LocationResult?,
    contactCount: Int,
    isSosActive: Boolean
) {
    val (bgColor, contentColor, icon, text) = when {
        isSosActive -> Quadruple(
            Color(0xFFD32F2F),
            Color.White,
            Icons.Default.Warning,
            "🚨 ACTIVE SOS EMERGENCY SESSION IN PROGRESS"
        )
        contactCount == 0 -> Quadruple(
            Color(0xFFFFF3E0),
            Color(0xFFE65100),
            Icons.Default.Warning,
            "⚠️ No Emergency Contacts Configured! Add contacts in Contacts tab."
        )
        location?.isFallback == true -> Quadruple(
            Color(0xFFFFF8E1),
            Color(0xFFF57F17),
            Icons.Default.GpsFixed,
            "📍 GPS Signal Weak / Permission Needed. Standard Fallback Active."
        )
        else -> Quadruple(
            Color(0xFFE8F5E9),
            Color(0xFF2E7D32),
            Icons.Default.CheckCircle,
            "✅ Ready — GPS High Accuracy Active • $contactCount Contacts Ready"
        )
    }

    Row(
        modifier = modifier
            .testTag("status_banner")
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        Icon(
            imageVector = icon,
            contentDescription = "Status Icon",
            tint = contentColor,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = text,
            color = contentColor,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.weight(1f)
        )
    }
}

private data class Quadruple<A, B, C, D>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D
)
