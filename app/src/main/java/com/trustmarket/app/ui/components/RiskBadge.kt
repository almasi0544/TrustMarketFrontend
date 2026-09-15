package com.trustmarket.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

fun riskColor(riskLevel: String): Color = when (riskLevel.uppercase()) {
    "LOW" -> Color(0xFF2E7D32)
    "MODERATE" -> Color(0xFFF5A623)
    "ELEVATED" -> Color(0xFFE67E22)
    else -> Color(0xFFE53935) // HIGH
}

@Composable
fun RiskBadge(riskLevel: String, trustScore: Int) {
    val color = riskColor(riskLevel)
    Row(
        modifier = Modifier
            .background(color.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = "${riskLevel.uppercase()}  $trustScore",
            color = color,
            style = MaterialTheme.typography.labelSmall
        )
    }
}