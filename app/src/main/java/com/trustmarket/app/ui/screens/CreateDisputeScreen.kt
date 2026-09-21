package com.trustmarket.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp
import com.trustmarket.app.network.DisputeCreateRequest
import com.trustmarket.app.network.RetrofitClient
import kotlinx.coroutines.launch
import com.trustmarket.app.network.friendlyErrorMessage

private val TrustTeal = Color(0xFF1F4E5F)

@Composable
fun CreateDisputeScreen(token: String?, transactionId: Int, onSubmitted: () -> Unit, onCancel: () -> Unit) {
    var reason by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Column(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxWidth().background(TrustTeal).padding(24.dp)) {
            Text("Report a Problem", color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.headlineMedium)
        }

        Column(modifier = Modifier.padding(24.dp)) {
            Text("What went wrong?", style = MaterialTheme.typography.labelLarge)
            Spacer(Modifier.height(4.dp))
            OutlinedTextField(
                value = reason,
                onValueChange = { reason = it },
                modifier = Modifier.fillMaxWidth().height(140.dp)
            )

            Spacer(Modifier.height(20.dp))
            if (error != null) {
                Text(error!!, color = MaterialTheme.colorScheme.error)
                Spacer(Modifier.height(8.dp))
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF0F8FB), RoundedCornerShape(12.dp))
                    .padding(14.dp)
            ) {
                Text(
                    "🔒 Escrow funds will remain held during the dispute period. Most disputes are resolved within 5 business days.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
            Spacer(Modifier.height(16.dp))

            Button(
                onClick = {
                    if (token == null) return@Button
                    loading = true
                    error = null
                    scope.launch {
                        try {
                            RetrofitClient.api.createDispute(
                                DisputeCreateRequest(transaction_id = transactionId, reason = reason, description = ""),
                                "Bearer $token"
                            )
                            onSubmitted()
                        } catch (e: Exception) {
                            error = friendlyErrorMessage(e)
                        } finally {
                            loading = false
                        }
                    }
                },
                enabled = !loading,
                colors = ButtonDefaults.buttonColors(containerColor = TrustTeal),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (loading) "Submitting..." else "Create Dispute")
            }

            Spacer(Modifier.height(12.dp))
            TextButton(onClick = onCancel, modifier = Modifier.fillMaxWidth()) { Text("Cancel") }
        }
    }
}