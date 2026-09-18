package com.trustmarket.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.trustmarket.app.network.RetrofitClient
import kotlinx.coroutines.launch

private val TrustTeal = Color(0xFF1F4E5F)
private val LEVELS = listOf("unverified", "email_verified", "phone_verified", "identity_verified", "business_verified")

private data class VerificationItem(
    val id: String,
    val label: String,
    val desc: String,
    val icon: String,
    val implemented: Boolean
)

private val ITEMS = listOf(
    VerificationItem("email", "Email Address", "Confirm your email to receive notifications", "✉", implemented = true),
    VerificationItem("phone", "Phone Number", "Add an extra layer of account security", "📱", implemented = false),
    VerificationItem("identity", "Identity (ID)", "Upload a government-issued photo ID", "🪪", implemented = false),
    VerificationItem("business", "Business", "Verify your business for a higher trust score", "🏢", implemented = false),
)

@Composable
fun VerificationScreen(token: String?, onBack: () -> Unit) {
    var level by remember { mutableStateOf("unverified") }
    var loading by remember { mutableStateOf(true) }
    var verifying by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(token) {
        if (token == null) return@LaunchedEffect
        try {
            level = RetrofitClient.api.getMyVerification("Bearer $token").level
        } catch (e: Exception) {
            error = "Failed to load: ${e.message}"
        } finally {
            loading = false
        }
    }

    val levelIndex = LEVELS.indexOf(level).coerceAtLeast(0)
    val pct = (levelIndex.toFloat() / (LEVELS.size - 1).toFloat()) * 100f

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth().background(TrustTeal).padding(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Verification Center", color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.headlineMedium)
        }

        if (loading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            return@Column
        }

        Column(modifier = Modifier.padding(20.dp)) {
            // Progress card
            Card(shape = RoundedCornerShape(16.dp)) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text("Verification Progress", fontWeight = FontWeight.Bold)
                            Text("Level: ${level.replace("_", " ")}", color = Color.Gray, style = MaterialTheme.typography.bodySmall)
                        }
                        Text("${pct.toInt()}%", color = TrustTeal, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.headlineSmall)
                    }
                    Spacer(Modifier.height(12.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .background(Color(0xFFE4E8EB), RoundedCornerShape(8.dp))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(pct / 100f)
                                .fillMaxHeight()
                                .background(
                                    Brush.horizontalGradient(listOf(TrustTeal, Color(0xFF5FA3BC))),
                                    RoundedCornerShape(8.dp)
                                )
                        )
                    }
                    Spacer(Modifier.height(10.dp))
                    Text(
                        "Higher verification increases your Trust Score and reduces your calculated risk level for buyers and sellers.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            if (error != null) {
                Text(error!!, color = MaterialTheme.colorScheme.error)
                Spacer(Modifier.height(8.dp))
            }

            ITEMS.forEach { item ->
                val isVerified = when (item.id) {
                    "email" -> levelIndex >= 1
                    "phone" -> levelIndex >= 2
                    "identity" -> levelIndex >= 3
                    "business" -> levelIndex >= 4
                    else -> false
                }

                Card(shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp)) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .background(
                                    if (isVerified) Color(0xFFDCFCE7) else Color(0xFFF3F4F6),
                                    RoundedCornerShape(12.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(item.icon, style = MaterialTheme.typography.titleLarge)
                        }
                        Spacer(Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(item.label, fontWeight = FontWeight.Bold)
                            Text(item.desc, color = Color.Gray, style = MaterialTheme.typography.bodySmall)
                            Spacer(Modifier.height(4.dp))
                            Text(
                                if (isVerified) "✓ VERIFIED" else if (item.implemented) "✗ NOT VERIFIED" else "COMING SOON",
                                color = if (isVerified) Color(0xFF16A34A) else if (item.implemented) Color(0xFFDC2626) else Color.Gray,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        if (!isVerified && item.implemented) {
                            Button(
                                onClick = {
                                    if (token == null) return@Button
                                    verifying = true
                                    scope.launch {
                                        try {
                                            level = RetrofitClient.api.verifyMyEmail("Bearer $token").level
                                        } catch (e: Exception) {
                                            error = "Verification failed: ${e.message}"
                                        } finally {
                                            verifying = false
                                        }
                                    }
                                },
                                enabled = !verifying,
                                colors = ButtonDefaults.buttonColors(containerColor = TrustTeal)
                            ) {
                                Text(if (verifying) "..." else "Verify")
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(8.dp))
            Box(
                modifier = Modifier.fillMaxWidth().background(Color(0xFFF0F8FB), RoundedCornerShape(12.dp)).padding(14.dp)
            ) {
                Text(
                    "🔒 Verification documents are securely stored and encrypted. They are never shared with buyers or sellers directly.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
        }
    }
}