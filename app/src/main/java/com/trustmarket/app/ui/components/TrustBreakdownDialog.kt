package com.trustmarket.app.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.material3.HorizontalDivider

private val TrustTeal = Color(0xFF1F4E5F)

@Suppress("Unused")
@Composable
fun TrustBreakdownDialog(
    trustScore: Int,
    verifiedLevel: String,
    completedTxCount: Int,
    disputeRatePct: Double,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Trust Score Analysis", fontWeight = FontWeight.Bold)
                Surface(
                    color = when {
                        trustScore >= 80 -> Color(0xFFDCFCE7)
                        trustScore >= 50 -> Color(0xFFFEF9C3)
                        else -> Color(0xFFFEE2E2)
                    },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "$trustScore/100",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontWeight = FontWeight.Bold,
                        color = when {
                            trustScore >= 80 -> Color(0xFF15803D)
                            trustScore >= 50 -> Color(0xFFA16207)
                            else -> Color(0xFFB91C1C)
                        }
                    )
                }
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Scores are dynamically generated using transaction history, dispute ratios, and identity verification.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 8.dp),
                    thickness = 1.dp,
                    color = Color.LightGray
                )

                ScoreFactorRow(
                    label = "Identity Verification",
                    value = verifiedLevel.replace("_", " ").uppercase(),
                    isPositive = verifiedLevel != "unverified"
                )

                ScoreFactorRow(
                    label = "Successful Escrows",
                    value = "$completedTxCount Completed",
                    isPositive = completedTxCount > 0
                )

                ScoreFactorRow(
                    label = "Dispute Rate",
                    value = "${"%.1f".format(disputeRatePct)}%",
                    isPositive = disputeRatePct < 5.0
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Got It", color = TrustTeal, fontWeight = FontWeight.Bold)
            }
        },
        shape = RoundedCornerShape(16.dp)
    )
}

@Composable
private fun ScoreFactorRow(
    label: String,
    value: String,
    isPositive: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Text(
            text = value,
            fontWeight = FontWeight.Bold,
            color = if (isPositive) Color(0xFF16A34A) else Color(0xFFDC2626),
            style = MaterialTheme.typography.bodyMedium
        )
    }
}