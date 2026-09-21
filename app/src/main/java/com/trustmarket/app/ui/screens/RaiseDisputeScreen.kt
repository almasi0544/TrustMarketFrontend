package com.trustmarket.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.trustmarket.app.network.DisputeCreateRequest
import com.trustmarket.app.network.RetrofitClient
import kotlinx.coroutines.launch
import com.trustmarket.app.network.friendlyErrorMessage

private val TrustTeal = Color(0xFF1F4E5F)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RaiseDisputeScreen(
    transactionId: Int,
    onBack: () -> Unit,
    onDisputeSubmitted: () -> Unit
) {
    val reasons = listOf(
        "ITEM_NOT_RECEIVED",
        "NOT_AS_DESCRIBED",
        "DAMAGED_ITEM",
        "WRONG_ITEM",
        "OTHER"
    )

    var selectedReason by remember { mutableStateOf(reasons.first()) }
    var description by remember { mutableStateOf("") }
    var dropdownExpanded by remember { mutableStateOf(false) }
    var submitting by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {
        TextButton(onClick = onBack) {
            Text("← Cancel", color = TrustTeal, fontWeight = FontWeight.Bold)
        }

        Spacer(Modifier.height(8.dp))
        Text(
            text = "Raise a Dispute",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Transaction #$transactionId",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray
        )

        Spacer(Modifier.height(16.dp))

        // Escrow Freeze Notice Banner
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFFEF3C7), RoundedCornerShape(12.dp))
                .padding(14.dp)
        ) {
            Column {
                Text(
                    "⚖ Escrow Hold Notice",
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF92400E)
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "Opening a dispute pauses funds transfer in escrow. Our Trust & Safety team will review the evidence before releasing funds.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF78350F)
                )
            }
        }

        Spacer(Modifier.height(20.dp))

        if (error != null) {
            Text(error!!, color = MaterialTheme.colorScheme.error)
            Spacer(Modifier.height(10.dp))
        }

        // Reason Dropdown
        ExposedDropdownMenuBox(
            expanded = dropdownExpanded,
            onExpandedChange = { dropdownExpanded = !dropdownExpanded }
        ) {
            OutlinedTextField(
                value = selectedReason.replace("_", " "),
                onValueChange = {},
                readOnly = true,
                label = { Text("Reason for Dispute") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropdownExpanded) },
                modifier = Modifier.menuAnchor().fillMaxWidth()
            )
            ExposedDropdownMenu(
                expanded = dropdownExpanded,
                onDismissRequest = { dropdownExpanded = false }
            ) {
                reasons.forEach { r ->
                    DropdownMenuItem(
                        text = { Text(r.replace("_", " ")) },
                        onClick = {
                            selectedReason = r
                            dropdownExpanded = false
                        }
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Describe the issue in detail") },
            minLines = 4,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(24.dp))

        Button(
            onClick = {
                if (description.isBlank()) {
                    error = "Please provide details about your issue."
                    return@Button
                }

                submitting = true
                scope.launch {
                    try {
                        RetrofitClient.api.createDispute(
                            DisputeCreateRequest(
                                transaction_id = transactionId,
                                reason = selectedReason,
                                description = description
                            )
                        )
                        onDisputeSubmitted()
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
            else Text("Submit Dispute", fontWeight = FontWeight.Bold)
        }
    }
}