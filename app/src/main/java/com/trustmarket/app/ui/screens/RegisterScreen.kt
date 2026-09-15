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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.trustmarket.app.network.RegisterRequest
import com.trustmarket.app.network.RetrofitClient
import kotlinx.coroutines.launch

private val TrustTeal = Color(0xFF1F4E5F)

@Composable
fun RegisterScreen(onRegisterSuccess: () -> Unit, onGoToLogin: () -> Unit) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Column(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxWidth().background(TrustTeal).padding(24.dp)
        ) {
            Text("✦ TrustMarket", color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(16.dp))
            Text("Create account", color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(4.dp))
            Text("Join thousands of trusted buyers & sellers", color = Color.White.copy(alpha = 0.85f))
        }

        Column(modifier = Modifier.fillMaxWidth().padding(24.dp)) {
            Text("Email address", style = MaterialTheme.typography.labelLarge)
            Spacer(Modifier.height(4.dp))
            OutlinedTextField(value = email, onValueChange = { email = it }, placeholder = { Text("you@example.com") }, modifier = Modifier.fillMaxWidth())

            Spacer(Modifier.height(16.dp))
            Text("Create password", style = MaterialTheme.typography.labelLarge)
            Spacer(Modifier.height(4.dp))
            OutlinedTextField(value = password, onValueChange = { password = it }, placeholder = { Text("Minimum 8 characters") }, visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth())

            Spacer(Modifier.height(16.dp))
            Text("Confirm password", style = MaterialTheme.typography.labelLarge)
            Spacer(Modifier.height(4.dp))
            OutlinedTextField(value = confirmPassword, onValueChange = { confirmPassword = it }, placeholder = { Text("Re-enter your password") }, visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth())

            Spacer(Modifier.height(16.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(TrustTeal.copy(alpha = 0.08f), RoundedCornerShape(8.dp))
                    .padding(12.dp)
            ) {
                Text(
                    "By creating an account you agree to our Terms of Service and acknowledge that your Trust Score will be publicly visible to buyers and sellers.",
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(Modifier.height(16.dp))
            if (error != null) {
                Text(error!!, color = MaterialTheme.colorScheme.error)
                Spacer(Modifier.height(8.dp))
            }

            Button(
                onClick = {
                    error = null
                    if (password != confirmPassword) {
                        error = "Passwords do not match"
                        return@Button
                    }
                    loading = true
                    scope.launch {
                        try {
                            RetrofitClient.api.register(RegisterRequest(email, password))
                            onRegisterSuccess()
                        } catch (e: Exception) {
                            error = "Registration failed: ${e.message}"
                        } finally {
                            loading = false
                        }
                    }
                },
                enabled = !loading,
                colors = ButtonDefaults.buttonColors(containerColor = TrustTeal),
                modifier = Modifier.fillMaxWidth().height(52.dp)
            ) {
                Text(if (loading) "Creating..." else "Create Account")
            }

            Spacer(Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                Text("Already have an account? ")
                Text("Log In", color = TrustTeal, fontWeight = FontWeight.Bold, modifier = Modifier.clickable(onClick = onGoToLogin))
            }
        }
    }
}