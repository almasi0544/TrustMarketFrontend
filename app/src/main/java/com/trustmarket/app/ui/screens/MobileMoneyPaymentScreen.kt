package com.trustmarket.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.trustmarket.app.network.MobileMoneyPaymentRequest
import com.trustmarket.app.network.RetrofitClient
import com.trustmarket.app.network.friendlyErrorMessage
import kotlinx.coroutines.launch

private val TrustTeal = Color(0xFF1F4E5F)

@Composable
fun MobileMoneyPaymentScreen(
    token: String?,
    transactionId: Int,
    onInitiated: () -> Unit,
    onCancel: () -> Unit
) {
    var phoneNumber by remember { mutableStateOf("255") }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    Column(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxWidth().background(TrustTeal).padding(24.dp)) {
            Text("Mobile Money Payment", color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.headlineMedium)
        }

        Column(modifier = Modifier.padding(24.dp)) {
            Text(
                "Enter the phone number that will receive the PIN prompt. Approve it there to complete payment — TrustMarket never sees or stores your PIN.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )
            Spacer(Modifier.height(20.dp))

            OutlinedTextField(
                value = phoneNumber,
                onValueChange = { phoneNumber = it.filter { c -> c.isDigit() } },
                label = { Text("Phone number (e.g. 255712345678)") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(20.dp))
            if (error != null) {
                Text(error!!, color = MaterialTheme.colorScheme.error)
                Spacer(Modifier.height(8.dp))
            }

            Button(
                onClick = {
                    if (token == null) return@Button
                    if (phoneNumber.length < 9) {
                        error = "Enter a valid phone number with country code."
                        return@Button
                    }
                    loading = true
                    error = null
                    scope.launch {
                        try {
                            RetrofitClient.api.payMobileMoney(
                                "Bearer $token",
                                transactionId,
                                MobileMoneyPaymentRequest(phone_number = phoneNumber)
                            )
                            onInitiated()
                        } catch (e: Exception) {
                            error = friendlyErrorMessage(e)
                        } finally {
                            loading = false
                        }
                    }
                },
                enabled = !loading,
                colors = ButtonDefaults.buttonColors(containerColor = TrustTeal),
                modifier = Modifier.fillMaxWidth().height(52.dp)
            ) {
                Text(if (loading) "Sending request..." else "Request Payment")
            }

            Spacer(Modifier.height(12.dp))
            TextButton(onClick = onCancel, modifier = Modifier.fillMaxWidth()) { Text("Cancel") }
        }
    }
}