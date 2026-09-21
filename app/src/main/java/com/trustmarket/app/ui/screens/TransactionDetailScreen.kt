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
import com.trustmarket.app.network.TransactionOut
import com.trustmarket.app.network.CategoryOut
import kotlinx.coroutines.launch
import com.trustmarket.app.network.friendlyErrorMessage
import com.trustmarket.app.util.formatPrice

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
    var categories by remember { mutableStateOf<List<CategoryOut>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var completing by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(transactionId, token) {
        if (token == null) return@LaunchedEffect
        try {
            val t = RetrofitClient.api.getTransaction("Bearer $token", transactionId)
            txn = t
            product = RetrofitClient.api.getProduct(t.product_id)
            categories = RetrofitClient.api.getCategories()
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
            error != null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text(error!!, color = MaterialTheme.colorScheme.error) }
            txn != null -> Column(modifier = Modifier.padding(24.dp)) {
                val t = txn!!
                Card(shape = RoundedCornerShape(12.dp)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(product?.title ?: "Product #${t.product_id}", fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(4.dp))
                        Text(formatPrice(t.amount), color = TrustTeal, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(8.dp))
                        product?.let { p ->
                            val catName = categories.find { it.id == p.category_id }?.name ?: "General"
                            DetailRow("Condition", p.condition?.replace("_", " ") ?: "unknown")
                            DetailRow("Category", catName)
                            DetailRow("Seller ID", "#${t.seller_id}")
                        }
                        Spacer(Modifier.height(8.dp))
                        Text("Status: ${t.status.uppercase()}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        Text("Created: ${t.created_at}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        t.completed_at?.let { Text("Completed: $it", style = MaterialTheme.typography.bodySmall, color = Color.Gray) }
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
                if (t.status == "pending") {
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

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = Color.Gray, style = MaterialTheme.typography.bodySmall)
        Text(value, fontWeight = FontWeight.Medium, style = MaterialTheme.typography.bodySmall)
    }
}