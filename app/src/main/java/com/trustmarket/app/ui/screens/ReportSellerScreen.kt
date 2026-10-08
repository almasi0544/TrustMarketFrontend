package com.trustmarket.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp
import com.trustmarket.app.network.ReportRequest
import com.trustmarket.app.network.RetrofitClient
import com.trustmarket.app.network.friendlyErrorMessage
import com.trustmarket.app.ui.components.MediaPickerField
import kotlinx.coroutines.launch

private val TrustTeal = Color(0xFF1F4E5F)

private val CATEGORIES = listOf(
    "scam_attempt", "fake_product", "fake_identity", "payment_fraud",
    "off_platform_pressure", "counterfeit", "harassment", "other"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportSellerScreen(token: String?, sellerId: Int, onSubmitted: () -> Unit, onCancel: () -> Unit) {
    var category by remember { mutableStateOf(CATEGORIES.first()) }
    var expanded by remember { mutableStateOf(false) }
    var description by remember { mutableStateOf("") }
    var evidenceUrl by remember { mutableStateOf<String?>(null) }
    var evidenceType by remember { mutableStateOf<String?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Column(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxWidth().background(TrustTeal).padding(24.dp)) {
            Text("Report Seller", color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.headlineMedium)
        }

        Column(modifier = Modifier.padding(24.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFFEE2E2), RoundedCornerShape(12.dp))
                    .padding(14.dp)
            ) {
                Text(
                    "Reports are reviewed by our Trust & Safety team. False reports may result in account action.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF991B1B)
                )
            }
            Spacer(Modifier.height(16.dp))

            Text("Category", style = MaterialTheme.typography.labelLarge)
            Spacer(Modifier.height(4.dp))
            ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
                OutlinedTextField(
                    value = category.replace("_", " ").uppercase(),
                    onValueChange = {},
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth().menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, enabled = true)
                )
                ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    CATEGORIES.forEach { c ->
                        DropdownMenuItem(
                            text = { Text(c.replace("_", " ").uppercase()) },
                            onClick = { category = c; expanded = false }
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
            Text("Description", style = MaterialTheme.typography.labelLarge)
            Spacer(Modifier.height(4.dp))
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                modifier = Modifier.fillMaxWidth().height(120.dp)
            )

            Spacer(Modifier.height(16.dp))
            Text("Evidence (required)", style = MaterialTheme.typography.labelLarge)
            Spacer(Modifier.height(4.dp))
            MediaPickerField(
                token = token,
                onUploaded = { url, type -> evidenceUrl = url; evidenceType = type; error = null },
                onError = { msg -> error = msg }
            )

            Spacer(Modifier.height(20.dp))
            if (error != null) {
                Text(error!!, color = MaterialTheme.colorScheme.error)
                Spacer(Modifier.height(8.dp))
            }

            Button(
                onClick = {
                    if (token == null) return@Button
                    val eUrl = evidenceUrl
                    val eType = evidenceType
                    if (description.isBlank()) {
                        error = "Please describe what happened"
                        return@Button
                    }
                    if (eUrl == null || eType == null) {
                        error = "Evidence (a screenshot or video) is required"
                        return@Button
                    }
                    loading = true
                    error = null
                    scope.launch {
                        try {
                            RetrofitClient.api.reportSeller(
                                ReportRequest(
                                    seller_id = sellerId,
                                    category = category,
                                    description = description,
                                    evidence_url = eUrl,
                                    evidence_type = eType
                                ),
                                "Bearer $token"
                            )
                            onSubmitted()
                        } catch (e: Exception) {
                            error = friendlyErrorMessage(e)
                        } finally {
                            loading = false
                        }
                    }
                },
                enabled = !loading,
                colors = ButtonDefaults.buttonColors(containerColor = TrustTeal),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (loading) "Submitting..." else "Submit Report")
            }

            Spacer(Modifier.height(12.dp))
            TextButton(onClick = onCancel, modifier = Modifier.fillMaxWidth()) { Text("Cancel") }
        }
    }
}