package com.trustmarket.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.trustmarket.app.network.ProfileOut
import com.trustmarket.app.network.RetrofitClient

private val TrustTeal = Color(0xFF1F4E5F)

@Composable
fun ProfileScreen(
    token: String?,
    onEdit: () -> Unit,
    onViewDisputes: () -> Unit,
    onViewVerification: () -> Unit
) {
    var profile by remember { mutableStateOf<ProfileOut?>(null) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(token) {
        if (token == null) return@LaunchedEffect
        try {
            profile = RetrofitClient.api.getMyProfile("Bearer $token")
        } catch (e: Exception) {
            error = "Failed to load profile: ${e.message}"
        } finally {
            loading = false
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxWidth().background(TrustTeal).padding(24.dp)) {
            Text("Profile", color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.headlineMedium)
        }

        when {
            loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            error != null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text(error!!, color = MaterialTheme.colorScheme.error) }
            else -> Column(modifier = Modifier.padding(24.dp)) {
                ProfileField("Full name", profile?.full_name ?: "Not set")
                ProfileField("Username", profile?.username ?: "Not set")
                ProfileField("Bio", profile?.bio ?: "Not set")
                ProfileField("Location", profile?.location ?: "Not set")

                Spacer(Modifier.height(24.dp))
                Button(
                    onClick = onEdit,
                    colors = ButtonDefaults.buttonColors(containerColor = TrustTeal),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Edit Profile")
                }

                Spacer(Modifier.height(12.dp))
                OutlinedButton(onClick = onViewDisputes, modifier = Modifier.fillMaxWidth()) {
                    Text("My Disputes")
                }
                Spacer(Modifier.height(8.dp))
                OutlinedButton(onClick = onViewVerification, modifier = Modifier.fillMaxWidth()) {
                    Text("Verification Center")
                }
            }
        }
    }
}

@Composable
private fun ProfileField(label: String, value: String) {
    Column(modifier = Modifier.padding(bottom = 16.dp)) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = Color.Gray)
        Text(value, style = MaterialTheme.typography.bodyLarge)
    }
}