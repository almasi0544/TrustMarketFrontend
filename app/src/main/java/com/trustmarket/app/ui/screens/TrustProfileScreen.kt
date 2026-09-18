package com.trustmarket.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import com.trustmarket.app.network.RetrofitClient
import com.trustmarket.app.network.TrustProfileOut
import com.trustmarket.app.ui.components.riskColor

private val TrustTeal = Color(0xFF1F4E5F)

@Composable
fun TrustProfileScreen(sellerId: Int, onBack: () -> Unit, onReportSeller: () -> Unit) {
    var trust by remember { mutableStateOf<TrustProfileOut?>(null) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(sellerId) {
        try {
            trust = RetrofitClient.api.getSellerTrust(sellerId)
        } catch (e: Exception) {
            error = "Failed to load trust profile: ${e.message}"
        } finally {
            loading = false
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth().background(TrustTeal).padding(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("←", color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.clickable(onClick = onBack))
            Spacer(Modifier.width(16.dp))
            Text("Seller Trust Profile", color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
        }

        when {
            loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            error != null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text(error!!, color = MaterialTheme.colorScheme.error) }
            trust != null -> TrustProfileContent(trust!!, onReportSeller = onReportSeller)
        }
    }
}

@Composable
private fun TrustProfileContent(trust: TrustProfileOut, onReportSeller: () -> Unit) {
    val color = riskColor(trust.risk_level)

    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 20.dp)) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(TrustTeal, RoundedCornerShape(50)),
                contentAlignment = Alignment.Center
            ) {
                Text(trust.email.take(2).uppercase(), color = Color.White, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.width(14.dp))
            Column {
                Text(trust.email, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                Text("${trust.account_age_days} days on TrustMarket", color = Color.Gray, style = MaterialTheme.typography.bodySmall)
            }
        }

        Column(
            modifier = Modifier.fillMaxWidth().background(color.copy(alpha = 0.1f), RoundedCornerShape(20.dp)).padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("${trust.trust_score}", style = MaterialTheme.typography.displayLarge, fontWeight = FontWeight.Bold, color = color)
            Text("TRUST SCORE", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            Spacer(Modifier.height(8.dp))
            Box(modifier = Modifier.background(Color.White, RoundedCornerShape(20.dp)).padding(horizontal = 14.dp, vertical = 6.dp)) {
                Text("${trust.risk_level} RISK", color = color, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(Modifier.height(24.dp))
        Text("EVIDENCE BREAKDOWN", style = MaterialTheme.typography.labelMedium, color = Color.Gray, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))

        EvidenceItem("Account age", "${trust.account_age_days} days", trust.account_age_days >= 90)
        EvidenceItem("Total transactions", "${trust.total_transactions} completed", trust.completed_transactions > 0)
        EvidenceItem("Completion rate", "${(trust.completion_rate * 100).toInt()}%", trust.completion_rate >= 0.8)
        EvidenceItem("Disputes raised", "${trust.disputes_raised}", trust.disputes_raised == 0)
        EvidenceItem(
            "Disputes resolved",
            "${trust.disputes_resolved} of ${trust.disputes_raised} resolved",
            trust.disputes_raised == 0 || trust.disputes_resolved == trust.disputes_raised
        )
        EvidenceItem("Reports filed", "${trust.reports_count}", trust.reports_count == 0)
        EvidenceItem(
            "Identity verified",
            trust.verification_level.replace("_", " "),
            trust.verification_level in listOf("identity_verified", "business_verified")
        )
        EvidenceItem(
            "Email verified",
            if (trust.verification_level != "unverified") "Verified" else "Not verified",
            trust.verification_level != "unverified"
        )

        Spacer(Modifier.height(20.dp))
        Box(modifier = Modifier.fillMaxWidth().background(Color(0xFFFEF9C3), RoundedCornerShape(12.dp)).padding(14.dp)) {
            Text(
                "⚠ This trust score is algorithmically calculated. It indicates risk level based on historical data — not a guarantee of safe or unsafe trading.",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF854D0E)
            )
        }

        Spacer(Modifier.height(16.dp))
        OutlinedButton(onClick = onReportSeller, modifier = Modifier.fillMaxWidth()) {
            Text("Report This Seller")
        }
    }
}

@Composable
private fun EvidenceItem(label: String, value: String, good: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(label, fontWeight = FontWeight.Medium, style = MaterialTheme.typography.bodyMedium)
            Text(value, color = Color.Gray, style = MaterialTheme.typography.bodySmall)
        }
        Text(
            if (good) "✓" else "⚠",
            color = if (good) Color(0xFF16A34A) else Color(0xFFDC2626),
            fontWeight = FontWeight.Bold
        )
    }
}