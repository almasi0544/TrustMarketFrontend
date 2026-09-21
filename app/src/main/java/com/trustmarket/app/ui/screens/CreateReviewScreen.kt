package com.trustmarket.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.trustmarket.app.network.ReviewCreateRequest
import com.trustmarket.app.network.RetrofitClient
import com.trustmarket.app.network.friendlyErrorMessage
import kotlinx.coroutines.launch

private val TrustTeal = Color(0xFF1F4E5F)

@Composable
fun CreateReviewScreen(
    transactionId: Int,
    onBack: () -> Unit,
    onReviewSubmitted: () -> Unit
) {
    var rating by remember { mutableIntStateOf(5) }
    var comment by remember { mutableStateOf("") }
    var submitting by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    Column(modifier = Modifier.fillMaxSize().padding(20.dp)) {
        TextButton(onClick = onBack) { Text("← Cancel", color = TrustTeal, fontWeight = FontWeight.Bold) }

        Spacer(Modifier.height(12.dp))
        Text("Leave a Review", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Text("Transaction #${transactionId}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)

        Spacer(Modifier.height(24.dp))

        // Star Rating Selector
        Row(
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth().padding(12.dp)
        ) {
            (1..5).forEach { star ->
                Text(
                    text = if (star <= rating) "★" else "☆",
                    style = MaterialTheme.typography.displayMedium,
                    color = Color(0xFFEAB308),
                    modifier = Modifier
                        .padding(horizontal = 6.dp)
                        .clickable { rating = star }
                )
            }
        }

        Spacer(Modifier.height(20.dp))

        OutlinedTextField(
            value = comment,
            onValueChange = { comment = it },
            label = { Text("Write your review") },
            minLines = 4,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(24.dp))

        Button(
            onClick = {
                if (comment.isBlank()) {
                    error = "Please enter a comment."
                    return@Button
                }
                submitting = true
                scope.launch {
                    try {
                        RetrofitClient.api.createReview(
                            ReviewCreateRequest(
                                transaction_id = transactionId,
                                rating = rating,
                                comment = comment
                            )
                        )
                        onReviewSubmitted()
                    } catch (e: Exception) {
                        error = friendlyErrorMessage(e)
                    } finally {
                        submitting = false
                    }
                }
            },
            enabled = !submitting,
            colors = ButtonDefaults.buttonColors(containerColor = TrustTeal),
            modifier = Modifier.fillMaxWidth().height(50.dp)
        ) {
            if (submitting) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
            else Text("Submit Review", fontWeight = FontWeight.Bold)
        }
    }
}