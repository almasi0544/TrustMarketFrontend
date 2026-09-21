package com.trustmarket.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

private val TrustTeal = Color(0xFF1F4E5F)
private val CompletedGreen = Color(0xFF16A34A)
private val DisputedAmber = Color(0xFFD97706)

data class TimelineStep(
    val title: String,
    val description: String,
    val isCompleted: Boolean,
    val isCurrent: Boolean
)

@Composable
fun EscrowTimeline(
    status: String,
    modifier: Modifier = Modifier
) {
    val steps = getStepsForStatus(status)

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "Escrow Progress",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        steps.forEachIndexed { index, step ->
            Row(
                verticalAlignment = Alignment.Top,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Left Column: Dot and Connecting Line
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.width(28.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .clip(CircleShape)
                            .background(
                                when {
                                    step.isCompleted -> CompletedGreen
                                    step.isCurrent -> TrustTeal
                                    else -> Color.LightGray
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (step.isCompleted) {
                            Text("✓", color = Color.White, style = MaterialTheme.typography.labelSmall)
                        }
                    }

                    if (index < steps.size - 1) {
                        Box(
                            modifier = Modifier
                                .width(2.dp)
                                .height(36.dp)
                                .background(if (step.isCompleted) CompletedGreen else Color.LightGray)
                        )
                    }
                }

                Spacer(Modifier.width(12.dp))

                // Right Column: Step Text Info
                Column(modifier = Modifier.padding(bottom = 12.dp)) {
                    Text(
                        text = step.title,
                        fontWeight = if (step.isCurrent) FontWeight.Bold else FontWeight.Medium,
                        color = if (step.isCurrent) TrustTeal else Color.Unspecified
                    )
                    Text(
                        text = step.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
            }
        }
    }
}

private fun getStepsForStatus(status: String): List<TimelineStep> {
    val s = status.lowercase()
    val isDisputed = s == "disputed"
    val isCompleted = s == "completed"

    return listOf(
        TimelineStep(
            title = "1. Transaction Initiated",
            description = "Buyer agreed to terms and committed funds.",
            isCompleted = true,
            isCurrent = s == "pending"
        ),
        TimelineStep(
            title = "2. Escrow Lock",
            description = "Payment secured in TrustMarket vault.",
            isCompleted = s in listOf("funded", "shipped", "delivered", "completed"),
            isCurrent = s == "funded"
        ),
        TimelineStep(
            title = "3. Buyer Inspection",
            description = "Item received; awaiting buyer approval or dispute.",
            isCompleted = isCompleted,
            isCurrent = s in listOf("shipped", "delivered") || isDisputed
        ),
        TimelineStep(
            title = if (isDisputed) "4. Dispute Under Review" else "4. Funds Released",
            description = if (isDisputed) "Trust & Safety team reviewing evidence." else "Seller paid; transaction finalized.",
            isCompleted = isCompleted,
            isCurrent = isCompleted || isDisputed
        )
    )
}