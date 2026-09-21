package com.trustmarket.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.trustmarket.app.network.RetrofitClient
import kotlinx.coroutines.launch
import com.trustmarket.app.network.friendlyErrorMessage

private val TrustTeal = Color(0xFF1F4E5F)

@Composable
fun LoginScreen(onLoginSuccess: (String) -> Unit, onGoToRegister: () -> Unit) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Column(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxWidth().background(TrustTeal).padding(32.dp)) {
            Text("✦ TrustMarket", color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(16.dp))
            Text("Welcome back", color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(4.dp))
            Text("Log in to your account", color = Color.White.copy(alpha = 0.85f))
        }

        Column(modifier = Modifier.fillMaxWidth().padding(24.dp)) {
            Text("Email address", style = MaterialTheme.typography.labelLarge)
            Spacer(Modifier.height(4.dp))
            OutlinedTextField(value = email, onValueChange = { email = it }, placeholder = { Text("you@example.com") }, modifier = Modifier.fillMaxWidth())

            Spacer(Modifier.height(16.dp))
            Text("Password", style = MaterialTheme.typography.labelLarge)
            Spacer(Modifier.height(4.dp))
            OutlinedTextField(value = password, onValueChange = { password = it }, visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth())

            Spacer(Modifier.height(8.dp))
            Text("Forgot password?", color = TrustTeal, modifier = Modifier.align(Alignment.End))

            Spacer(Modifier.height(16.dp))
            if (error != null) {
                Text(error!!, color = MaterialTheme.colorScheme.error)
                Spacer(Modifier.height(8.dp))
            }

            Button(
                onClick = {
                    error = null
                    loading = true
                    scope.launch {
                        try {
                            val response = RetrofitClient.api.login(email, password)
                            onLoginSuccess(response.access_token)
                        } catch (e: Exception) {
                            error = friendlyErrorMessage(e)
                        } finally {
                            loading = false
                        }
                    }
                },
                enabled = !loading,
                colors = ButtonDefaults.buttonColors(containerColor = TrustTeal),
                modifier = Modifier.fillMaxWidth().height(52.dp)
            ) {
                Text(if (loading) "Logging in..." else "Log In")
            }

            Spacer(Modifier.height(16.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(TrustTeal.copy(alpha = 0.08f), RoundedCornerShape(8.dp))
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Lock, contentDescription = null, tint = TrustTeal)
                Spacer(Modifier.width(8.dp))
                Text("All transactions are monitored for fraud. Trust scores help you make informed decisions.", style = MaterialTheme.typography.bodySmall)
            }

            Spacer(Modifier.height(24.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                Text("New to TrustMarket? ")
                Text("Create Account", color = TrustTeal, fontWeight = FontWeight.Bold, modifier = Modifier.clickable(onClick = onGoToRegister))
            }
        }
    }
}