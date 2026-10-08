package com.trustmarket.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.trustmarket.app.network.RetrofitClient
import com.trustmarket.app.network.friendlyErrorMessage
import kotlinx.coroutines.delay

private val TrustTeal = Color(0xFF1F4E5F)
private const val MAX_ATTEMPTS = 40
private const val POLL_INTERVAL_MS = 5000L

@Composable
fun PaymentPendingScreen(token: String?, transactionId: Int, onDone: () -> Unit) {
    var status by remember { mutableStateOf("pending") }
    var attempts by remember { mutableStateOf(0) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(transactionId, token) {
        if (token == null) return@LaunchedEffect
        while (status == "pending" && attempts < MAX_ATTEMPTS) {
            try {
                val result = RetrofitClient.api.getPaymentStatus("Bearer $token", transactionId)
                status = result.payment_status ?: "pending"
            } catch (e: Exception) {
                error = friendlyErrorMessage(e)
            }
            attempts++
            if (status == "pending") delay(POLL_INTERVAL_MS)
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxWidth().background(TrustTeal).padding(24.dp)) {
            Text("Confirming Payment", color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.headlineMedium)
        }

        Box(modifier = Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                when (status) {
                    "successful" -> {
                        Text("✓", color = Color(0xFF16A34A), style = MaterialTheme.typography.displayLarge)
                        Spacer(Modifier.height(12.dp))
                        Text("Payment confirmed", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
                        Spacer(Modifier.height(20.dp))
                        Button(onClick = onDone, colors = ButtonDefaults.buttonColors(containerColor = TrustTeal)) { Text("View Purchase") }
                    }
                    "failed" -> {
                        Text("✗", color = Color(0xFFDC2626), style = MaterialTheme.typography.displayLarge)
                        Spacer(Modifier.height(12.dp))
                        Text("Payment failed or was declined", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
                        Spacer(Modifier.height(20.dp))
                        Button(onClick = onDone, colors = ButtonDefaults.buttonColors(containerColor = TrustTeal)) { Text("Back to Purchases") }
                    }
                    else -> {
                        CircularProgressIndicator(color = TrustTeal)
                        Spacer(Modifier.height(20.dp))
                        Text(
                            if (attempts >= MAX_ATTEMPTS) "Still waiting on confirmation — check My Purchases shortly."
                            else "Waiting for you to approve the PIN prompt on your phone…",
                            textAlign = TextAlign.Center,
                            color = Color.Gray
                        )
                        if (attempts >= MAX_ATTEMPTS) {
                            Spacer(Modifier.height(20.dp))
                            Button(onClick = onDone, colors = ButtonDefaults.buttonColors(containerColor = TrustTeal)) { Text("Back to Purchases") }
                        }
                    }
                }
                if (error != null) {
                    Spacer(Modifier.height(12.dp))
                    Text(error!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}