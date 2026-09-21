package com.trustmarket.app.ui.screens

import androidx.compose.foundation.background
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
import com.trustmarket.app.network.ProfileOut
import com.trustmarket.app.network.RetrofitClient
import com.trustmarket.app.network.TrustProfileOut
import com.trustmarket.app.network.friendlyErrorMessage
import com.trustmarket.app.ui.components.RiskBadge
import com.trustmarket.app.util.memberSince

private val TrustTeal = Color(0xFF1F4E5F)

@Composable
fun ProfileScreen(
    token: String?,
    onEdit: () -> Unit,
    onViewDisputes: () -> Unit,
    onViewVerification: () -> Unit,
    onViewTrustProfile: (Int) -> Unit, // Updated from () -> Unit to (Int) -> Unit
    onSignOut: () -> Unit
) {
    var profile by remember { mutableStateOf<ProfileOut?>(null) }
    var trust by remember { mutableStateOf<TrustProfileOut?>(null) }
    var myUserId by remember { mutableStateOf<Int?>(null) }
    var myUserCreatedAt by remember { mutableStateOf<String?>(null) }
    var completedPurchases by remember { mutableStateOf(0) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(token) {
        if (token == null) return@LaunchedEffect
        try {
            val me = RetrofitClient.api.getCurrentUser("Bearer $token")
            myUserId = me.id
            myUserCreatedAt = me.created_at
            profile = RetrofitClient.api.getMyProfile("Bearer $token")
            trust = RetrofitClient.api.getSellerTrust(me.id)
            val purchases = RetrofitClient.api.getMyTransactions("Bearer $token", role = "buyer")
            completedPurchases = purchases.count { it.status == "completed" }
        } catch (e: Exception) {
            error = friendlyErrorMessage(e)
        } finally {
            loading = false
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        if (loading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            return@Column
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(TrustTeal)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(50)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = (profile?.username ?: "??").take(2).uppercase(),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleLarge
                )
            }
            Spacer(Modifier.height(12.dp))
            Text(
                text = profile?.full_name?.ifBlank { null } ?: "Your Profile",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.headlineSmall
            )
            profile?.username?.ifBlank { null }?.let {
                Text(
                    text = "@$it",
                    color = Color.White.copy(alpha = 0.7f),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            Spacer(Modifier.height(10.dp))
            trust?.let { RiskBadge(riskLevel = it.risk_level, trustScore = it.trust_score) }
        }

        Column(modifier = Modifier.padding(20.dp)) {
            if (error != null) {
                Text(error!!, color = MaterialTheme.colorScheme.error)
                Spacer(Modifier.height(12.dp))
            }

            Card(shape = RoundedCornerShape(12.dp)) {
                Column {
                    ProfileStatRow("Member since", myUserCreatedAt?.let { memberSince(it) } ?: "")
                    ProfileStatRow("Completed sales", "${trust?.completed_transactions ?: 0}")
                    ProfileStatRow("Completed purchases", "$completedPurchases")
                    ProfileStatRow("Location", profile?.location?.ifBlank { null } ?: "Not set")
                }
            }

            Spacer(Modifier.height(16.dp))
            Text("BIO", style = MaterialTheme.typography.labelMedium, color = Color.Gray)
            Spacer(Modifier.height(6.dp))
            Card(shape = RoundedCornerShape(12.dp)) {
                Text(
                    text = profile?.bio?.ifBlank { null } ?: "No bio yet.",
                    modifier = Modifier.padding(14.dp),
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(Modifier.height(16.dp))
            Card(shape = RoundedCornerShape(12.dp)) {
                Column {
                    ProfileLinkRow("Edit Profile", onEdit)
                    ProfileLinkRow("Verification Center", onViewVerification)
                    myUserId?.let { id -> ProfileLinkRow("My Trust Profile") { onViewTrustProfile(id) } }
                    ProfileLinkRow("My Disputes", onViewDisputes)
                }
            }

            Spacer(Modifier.height(20.dp))
            OutlinedButton(
                onClick = onSignOut,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFDC2626)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Sign Out")
            }
        }
    }
}

@Composable
private fun ProfileStatRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = Color.Gray, style = MaterialTheme.typography.bodySmall)
        Text(value, fontWeight = FontWeight.Medium, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun ProfileLinkRow(label: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, fontWeight = FontWeight.Medium)
        Text("›", color = Color.Gray)
    }
}