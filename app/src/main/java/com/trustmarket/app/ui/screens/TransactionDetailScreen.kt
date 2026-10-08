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
import com.trustmarket.app.util.formatPrice
import kotlinx.coroutines.launch

private val TrustTeal = Color(0xFF1F4E5F)

@Composable
fun TransactionDetailScreen(
    token: String?,
    transactionId: Int,
    onBack: () -> Unit,
    onRaiseDispute: (Int) -> Unit,
    onReportSeller: (Int) -> Unit,
    onViewTrustProfile: (Int) -> Unit
) {
    var txn by remember { mutableStateOf<TransactionOut?>(null) }
    var product by remember { mutableStateOf<ProductOut?>(null) }
    var myUserId by remember { mutableStateOf<Int?>(null) }
    var loading by remember { mutableStateOf(true) }
    var completing by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var showShippingForm by remember { mutableStateOf(false) }
    var trackingRef by remember { mutableStateOf("") }
    var deliveryDate by remember { mutableStateOf("") }
    var savingShipping by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(transactionId, token) {
        if (token == null) return@LaunchedEffect
        try {
            val t = RetrofitClient.api.getTransaction("Bearer $token", transactionId)
            txn = t
            product = RetrofitClient.api.getProduct(t.product_id)
            myUserId = RetrofitClient.api.getCurrentUser("Bearer $token").id
        } catch (e: Exception) {
            error = friendlyErrorMessage(e)
        } finally {
            loading = false
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth().background(TrustTeal).padding(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Transaction Detail", color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
        }

        when {
            loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            error != null && txn == null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text(error!!, color = MaterialTheme.colorScheme.error) }
            txn != null -> {
                val t = txn!!
                val isSeller = myUserId == t.seller_id

                Column(modifier = Modifier.padding(24.dp)) {
                    Card(shape = RoundedCornerShape(12.dp)) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(product?.title ?: "Product #${t.product_id}", fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(4.dp))
                            Text(formatPrice(t.amount), color = TrustTeal, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(8.dp))
                            Text("Status: ${t.status.uppercase()}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        }
                    }

                    Spacer(Modifier.height(16.dp))
                    Card(shape = RoundedCornerShape(12.dp)) {
                        Column {
                            DetailRow("Item price", formatPrice(t.amount - t.buyer_protection_fee - t.shipping_cost))
                            DetailRow("Buyer protection fee", formatPrice(t.buyer_protection_fee))
                            DetailRow("Shipping", formatPrice(t.shipping_cost))
                            DetailRow("Payment method", t.payment_method.replace("_", " ").replaceFirstChar { it.uppercase() })
                            t.tracking_reference?.let { DetailRow("Tracking reference", it) }
                            t.expected_delivery_date?.let { DetailRow("Expected delivery", it) }
                        }
                    }

                    Spacer(Modifier.height(16.dp))
                    OutlinedButton(onClick = { onViewTrustProfile(t.seller_id) }, modifier = Modifier.fillMaxWidth()) {
                        Text("View Seller Trust Profile")
                    }

                    if (error != null) {
                        Spacer(Modifier.height(8.dp))
                        Text(error!!, color = MaterialTheme.colorScheme.error)
                    }

                    Spacer(Modifier.height(20.dp))

                    if (isSeller && t.tracking_reference == null) {
                        if (!showShippingForm) {
                            Button(
                                onClick = { showShippingForm = true },
                                colors = ButtonDefaults.buttonColors(containerColor = TrustTeal),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Add Shipping Info")
                            }
                        } else {
                            Card(shape = RoundedCornerShape(12.dp)) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    OutlinedTextField(
                                        value = trackingRef,
                                        onValueChange = { trackingRef = it },
                                        label = { Text("Tracking reference") },
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    Spacer(Modifier.height(10.dp))
                                    OutlinedTextField(
                                        value = deliveryDate,
                                        onValueChange = { deliveryDate = it },
                                        label = { Text("Expected delivery (YYYY-MM-DD)") },
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    Spacer(Modifier.height(12.dp))
                                    Button(
                                        onClick = {
                                            if (token == null) return@Button
                                            savingShipping = true
                                            scope.launch {
                                                try {
                                                    txn = RetrofitClient.api.updateShipping(
                                                        "Bearer $token", t.id,
                                                        ShippingUpdateRequest(trackingRef, deliveryDate)
                                                    )
                                                    showShippingForm = false
                                                } catch (e: Exception) {
                                                    error = friendlyErrorMessage(e)
                                                } finally {
                                                    savingShipping = false
                                                }
                                            }
                                        },
                                        enabled = !savingShipping,
                                        colors = ButtonDefaults.buttonColors(containerColor = TrustTeal),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(if (savingShipping) "Saving..." else "Save & Mark Shipped")
                                    }
                                }
                            }
                        }
                        Spacer(Modifier.height(12.dp))
                    }

                    if (t.status == "pending" && !isSeller) {
                        Button(
                            onClick = {
                                if (token == null) return@Button
                                completing = true
                                scope.launch {
                                    try {
                                        txn = RetrofitClient.api.completeTransaction(t.id, "Bearer $token")
                                    } catch (e: Exception) {
                                        error = friendlyErrorMessage(e)
                                    } finally {
                                        completing = false
                                    }
                                }
                            },
                            enabled = !completing,
                            colors = ButtonDefaults.buttonColors(containerColor = TrustTeal),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(if (completing) "..." else "Mark as Received / Complete")
                        }
                        Spacer(Modifier.height(12.dp))
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedButton(onClick = { onRaiseDispute(t.id) }, modifier = Modifier.weight(1f)) {
                            Text("Raise Dispute")
                        }
                        OutlinedButton(onClick = { onReportSeller(t.seller_id) }, modifier = Modifier.weight(1f)) {
                            Text("Report Problem")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = Color.Gray, style = MaterialTheme.typography.bodySmall)
        Text(value, fontWeight = FontWeight.Medium, style = MaterialTheme.typography.bodySmall)
    }
}