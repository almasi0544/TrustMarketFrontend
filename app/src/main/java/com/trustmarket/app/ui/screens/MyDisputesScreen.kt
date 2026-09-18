package com.trustmarket.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.trustmarket.app.network.DisputeOut
import com.trustmarket.app.network.RetrofitClient

private val TrustTeal = Color(0xFF1F4E5F)

private fun statusColor(status: String): Color = when (status) {
    "open" -> Color(0xFFF5A623)
    "seller_favored", "no_fault" -> Color(0xFF2E7D32)
    "buyer_favored" -> Color(0xFF1F4E5F)
    "partial_resolution" -> Color(0xFFE67E22)
    else -> Color.Gray
}

@Composable
fun MyDisputesScreen(token: String?, onBack: () -> Unit) {
    var disputes by remember { mutableStateOf<List<DisputeOut>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(token) {
        if (token == null) return@LaunchedEffect
        try {
            disputes = RetrofitClient.api.getMyDisputes("Bearer $token")
        } catch (e: Exception) {
            error = "Failed to load disputes: ${e.message}"
        } finally {
            loading = false
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth().background(TrustTeal).padding(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("←", color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.padding(end = 16.dp))
            Text("My Disputes", color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
        }

        when {
            loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            error != null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text(error!!, color = MaterialTheme.colorScheme.error) }
            disputes.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("No disputes yet") }
            else -> LazyColumn(contentPadding = PaddingValues(16.dp)) {
                items(disputes) { dispute -> DisputeCard(dispute) }
            }
        }
    }
}

@Composable
private fun DisputeCard(dispute: DisputeOut) {
    Card(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp), shape = RoundedCornerShape(12.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Transaction #${dispute.transaction_id}", fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(4.dp))
            Text(dispute.reason, style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .background(statusColor(dispute.status).copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(dispute.status.replace("_", " ").uppercase(), color = statusColor(dispute.status), style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}