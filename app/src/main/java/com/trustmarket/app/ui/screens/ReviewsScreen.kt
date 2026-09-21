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
import com.trustmarket.app.network.RetrofitClient
import com.trustmarket.app.network.ReviewOut
import com.trustmarket.app.network.friendlyErrorMessage

private val TrustTeal = Color(0xFF1F4E5F)

@Composable
fun ReviewsScreen(
    sellerId: Int,
    onBack: () -> Unit
) {
    var reviews by remember { mutableStateOf<List<ReviewOut>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(sellerId) {
        try {
            reviews = RetrofitClient.api.getSellerReviews(sellerId)
        } catch (e: Exception) {
            error = friendlyErrorMessage(e)
        } finally {
            loading = false
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = onBack) { Text("← Back", color = TrustTeal, fontWeight = FontWeight.Bold) }
            Spacer(Modifier.width(8.dp))
            Text("Seller Reviews", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }

        Spacer(Modifier.height(16.dp))

        when {
            loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            error != null -> Text(error!!, color = MaterialTheme.colorScheme.error)
            reviews.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("No reviews found for this seller.") }
            else -> LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(reviews) { r ->
                    Card(shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                Text("★".repeat(r.rating) + "☆".repeat(5 - r.rating), color = Color(0xFFEAB308), fontWeight = FontWeight.Bold)
                                Text(r.created_at.take(10), style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                            }
                            Spacer(Modifier.height(8.dp))
                            Text(r.comment, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
        }
    }
}