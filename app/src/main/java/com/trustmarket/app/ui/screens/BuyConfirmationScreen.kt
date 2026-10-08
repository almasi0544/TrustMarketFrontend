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
import com.trustmarket.app.network.*
import com.trustmarket.app.ui.components.RiskBadge
import com.trustmarket.app.util.formatPrice
import kotlinx.coroutines.launch

private val TrustTeal = Color(0xFF1F4E5F)
private val PAYMENT_METHODS = listOf(
    "cash_on_delivery" to "Cash on Delivery",
    "bank_transfer" to "Bank Transfer",
    "mobile_money" to "Mobile Money (M-Pesa / Tigo Pesa / Airtel)"
)

@Composable
fun BuyConfirmationScreen(token: String?, productId: Int, onConfirmed: (Int, String) -> Unit, onCancel: () -> Unit) {
    var product by remember { mutableStateOf<ProductOut?>(null) }
    var trust by remember { mutableStateOf<TrustProfileOut?>(null) }
    var paymentMethod by remember { mutableStateOf(PAYMENT_METHODS.first().first) }
    var expanded by remember { mutableStateOf(false) }
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
            val protectionFee = p.price * 0.01
            val total = p.price + protectionFee + p.shipping_cost

            Column(modifier = Modifier.padding(24.dp)) {
                Card(shape = RoundedCornerShape(12.dp)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("ORDER SUMMARY", style = MaterialTheme.typography.labelMedium, color = Color.Gray)
                        Spacer(Modifier.height(10.dp))
                        Text(p.title, fontWeight = FontWeight.Bold)
                        Text(p.condition?.replace("_", " ") ?: "unknown", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    }
                }

                Spacer(Modifier.height(16.dp))
                Card(shape = RoundedCornerShape(12.dp)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        PriceRow("Item price", formatPrice(p.price))
                        PriceRow("Buyer protection fee (1%)", formatPrice(protectionFee))
                        PriceRow("Shipping", formatPrice(p.shipping_cost))
                        Spacer(Modifier.height(8.dp))
                        PriceRow("Total", formatPrice(total), bold = true)
                    }
                }

                Spacer(Modifier.height(16.dp))
                trust?.let {
                    Card(shape = RoundedCornerShape(12.dp)) {
                        Row(
                            modifier = Modifier.padding(16.dp).fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Seller trust level", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                            RiskBadge(riskLevel = it.risk_level, trustScore = it.trust_score)
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                }

                Text("Payment method", style = MaterialTheme.typography.labelLarge)
                Spacer(Modifier.height(4.dp))
                @OptIn(ExperimentalMaterial3Api::class)
                ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
                    OutlinedTextField(
                        value = PAYMENT_METHODS.find { it.first == paymentMethod }?.second ?: "",
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier.fillMaxWidth().menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, enabled = true)
                    )
                    ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        PAYMENT_METHODS.forEach { (value, label) ->
                            DropdownMenuItem(text = { Text(label) }, onClick = { paymentMethod = value; expanded = false })
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
                                    TransactionCreateRequest(product_id = p.id, payment_method = paymentMethod),
                                    "Bearer $token"
                                )
                                onConfirmed(txn.id, paymentMethod)
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

@Composable
private fun PriceRow(label: String, value: String, bold: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal)
        Text(value, fontWeight = if (bold) FontWeight.Bold else FontWeight.Medium, color = if (bold) TrustTeal else Color.Unspecified)
    }
}