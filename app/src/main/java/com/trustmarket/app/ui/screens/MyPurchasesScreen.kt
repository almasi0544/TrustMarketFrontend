package com.trustmarket.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import com.trustmarket.app.network.ProductOut
import com.trustmarket.app.network.RetrofitClient
import com.trustmarket.app.network.TransactionOut
import com.trustmarket.app.network.friendlyErrorMessage
import com.trustmarket.app.util.formatPrice

private val TrustTeal = Color(0xFF1F4E5F)

private fun statusColor(status: String): Color = when (status) {
    "completed" -> Color(0xFF16A34A)
    "pending" -> Color(0xFFCA8A04)
    "disputed" -> Color(0xFFEA580C)
    "cancelled", "refunded" -> Color.Gray
    else -> Color.Gray
}

@Composable
fun MyPurchasesScreen(token: String?, onOpenTransaction: (Int) -> Unit) {
    var transactions by remember { mutableStateOf<List<TransactionOut>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(token) {
        if (token == null) return@LaunchedEffect
        try {
            transactions = RetrofitClient.api.getMyTransactions("Bearer $token", role = "buyer")
        } catch (e: Exception) {
            error = friendlyErrorMessage(e)
        } finally {
            loading = false
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxWidth().background(TrustTeal).padding(24.dp)) {
            Text("My Purchases", color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.headlineMedium)
        }

        when {
            loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            error != null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text(error!!, color = MaterialTheme.colorScheme.error) }
            transactions.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("No purchases yet") }
            else -> LazyColumn(contentPadding = PaddingValues(16.dp)) {
                items(transactions) { txn -> PurchaseCard(txn, onOpenTransaction) }
            }
        }
    }
}

@Composable
private fun PurchaseCard(txn: TransactionOut, onOpen: (Int) -> Unit) {
    var product by remember { mutableStateOf<ProductOut?>(null) }

    LaunchedEffect(txn.product_id) {
        try {
            product = RetrofitClient.api.getProduct(txn.product_id)
        } catch (e: Exception) {
            product = null
        }
    }

    Card(
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp).clickable { onOpen(txn.id) }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(product?.title ?: "Loading...", fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                Text(formatPrice(txn.amount), fontWeight = FontWeight.Bold, color = TrustTeal)
            }
            Spacer(Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .background(statusColor(txn.status).copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(txn.status.uppercase(), color = statusColor(txn.status), style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}