package com.trustmarket.app.ui.screens

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
import com.trustmarket.app.network.friendlyErrorMessage
import com.trustmarket.app.util.formatPrice
import kotlinx.coroutines.launch

private val TrustTeal = Color(0xFF1F4E5F)

@Composable
fun MyProductsScreen(token: String?, onCreateProduct: () -> Unit, onEditProduct: (Int) -> Unit) {
    var products by remember { mutableStateOf<List<ProductOut>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var initialError by remember { mutableStateOf<String?>(null) }
    var actionError by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    suspend fun reload() {
        if (token == null) return
        products = RetrofitClient.api.getMyProducts("Bearer $token")
    }

    LaunchedEffect(token) {
        try {
            reload()
        } catch (e: Exception) {
            initialError = friendlyErrorMessage(e)
        } finally {
            loading = false
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("My Products", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.headlineMedium)
            Button(onClick = onCreateProduct, colors = ButtonDefaults.buttonColors(containerColor = TrustTeal)) {
                Text("+ New")
            }
        }

        if (actionError != null) {
            Text(
                actionError!!,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp)
            )
        }

        when {
            loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            initialError != null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text(initialError!!, color = MaterialTheme.colorScheme.error) }
            products.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("No products listed yet") }
            else -> LazyColumn(contentPadding = PaddingValues(16.dp)) {
                items(products) { product ->
                    Card(shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(product.title, fontWeight = FontWeight.Bold)
                            Text("${formatPrice(product.price)} · ${product.status}", color = Color.Gray, style = MaterialTheme.typography.bodySmall)
                            Spacer(Modifier.height(10.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                OutlinedButton(onClick = { onEditProduct(product.id) }) { Text("Edit") }
                                OutlinedButton(
                                    onClick = {
                                        if (token == null) return@OutlinedButton
                                        actionError = null
                                        scope.launch {
                                            try {
                                                RetrofitClient.api.deleteProduct("Bearer $token", product.id)
                                                reload()
                                            } catch (e: Exception) {
                                                actionError = friendlyErrorMessage(e)
                                            }
                                        }
                                    }
                                ) { Text("Delete", color = Color(0xFFDC2626)) }
                            }
                        }
                    }
                }
            }
        }
    }
}