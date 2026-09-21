package com.trustmarket.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.trustmarket.app.network.ProductOut
import com.trustmarket.app.network.RetrofitClient
import com.trustmarket.app.network.TransactionCreateRequest
import kotlinx.coroutines.launch
import com.trustmarket.app.network.friendlyErrorMessage
import com.trustmarket.app.network.TrustProfileOut
import com.trustmarket.app.ui.components.RiskBadge
import com.trustmarket.app.util.formatPrice

private val TrustTeal = Color(0xFF1F4E5F)

@Composable
fun BuyConfirmationScreen(token: String?, productId: Int, onConfirmed: (Int) -> Unit, onCancel: () -> Unit) {
    var product by remember { mutableStateOf<ProductOut?>(null) }
    var trust by remember { mutableStateOf<TrustProfileOut?>(null) }
    var loading by remember { mutableStateOf(true) }
    var confirming by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(productId) {
        try {
            val p = RetrofitClient.api.getProduct(productId)
            product = p
            trust = RetrofitClient.api.getSellerTrust(p.seller_id)
        } catch (e: Exception) {
            error = friendlyErrorMessage(e)
        } finally {
            loading = false
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxWidth().background(TrustTeal).padding(24.dp)) {
            Text("Confirm Purchase", color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.headlineMedium)
        }

        if (loading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            return@Column
        }

        product?.let { p ->
            Column(modifier = Modifier.padding(24.dp)) {
                Card(shape = RoundedCornerShape(12.dp)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("ORDER SUMMARY", style = MaterialTheme.typography.labelMedium, color = Color.Gray)
                        Spacer(Modifier.height(10.dp))
                        Text(p.title, fontWeight = FontWeight.Bold)
                        Text(p.condition?.replace("_", " ") ?: "unknown", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        Spacer(Modifier.height(12.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Total", fontWeight = FontWeight.Bold)
                            Text(formatPrice(p.price), fontWeight = FontWeight.Bold, color = TrustTeal)
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))
                Card(shape = RoundedCornerShape(12.dp)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("RISK ASSESSMENT", style = MaterialTheme.typography.labelMedium, color = Color.Gray)
                        Spacer(Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Seller trust level", style = MaterialTheme.typography.bodyMedium)
                            trust?.let { RiskBadge(riskLevel = it.risk_level, trustScore = it.trust_score) }
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))
                Box(modifier = Modifier.fillMaxWidth().background(Color(0xFFF0F8FB), RoundedCornerShape(12.dp)).padding(14.dp)) {
                    Text(
                        "🔒 Your purchase is recorded and trackable. You can raise a dispute if there's a problem after purchase.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }

                Spacer(Modifier.height(20.dp))
                if (error != null) {
                    Text(error!!, color = MaterialTheme.colorScheme.error)
                    Spacer(Modifier.height(8.dp))
                }

                Button(
                    onClick = {
                        if (token == null) return@Button
                        confirming = true
                        error = null
                        scope.launch {
                            try {
                                val txn = RetrofitClient.api.createTransaction(
                                    TransactionCreateRequest(product_id = p.id),
                                    "Bearer $token"
                                )
                                onConfirmed(txn.id)
                            } catch (e: Exception) {
                                error = friendlyErrorMessage(e)
                            } finally {
                                confirming = false
                            }
                        }
                    },
                    enabled = !confirming,
                    colors = ButtonDefaults.buttonColors(containerColor = TrustTeal),
                    modifier = Modifier.fillMaxWidth().height(52.dp)
                ) {
                    Text(if (confirming) "Processing..." else "Confirm Purchase")
                }

                Spacer(Modifier.height(12.dp))
                TextButton(onClick = onCancel, modifier = Modifier.fillMaxWidth()) { Text("Cancel") }
            }
        }
    }
}