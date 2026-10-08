package com.trustmarket.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.trustmarket.app.network.ForgotPasswordRequest
import com.trustmarket.app.network.RetrofitClient
import com.trustmarket.app.network.friendlyErrorMessage
import kotlinx.coroutines.launch

private val TrustTeal = Color(0xFF1F4E5F)

@Composable
fun ForgotPasswordScreen(onCodeSent: () -> Unit, onCancel: () -> Unit) {
    var email by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    Column(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxWidth().background(TrustTeal).padding(24.dp)) {
            Text("Reset Password", color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.headlineMedium)
        }

        Column(modifier = Modifier.padding(24.dp)) {
            Text("Enter your account email and we'll send you a reset code.", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
            Spacer(Modifier.height(20.dp))
            OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email") }, modifier = Modifier.fillMaxWidth())

            Spacer(Modifier.height(20.dp))
            if (error != null) {
                Text(error!!, color = MaterialTheme.colorScheme.error)
                Spacer(Modifier.height(8.dp))
            }

            Button(
                onClick = {
                    loading = true
                    error = null
                    scope.launch {
                        try {
                            RetrofitClient.api.forgotPassword(ForgotPasswordRequest(email))
                            onCodeSent()
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
                Text(if (loading) "Sending..." else "Send Reset Code")
            }

            Spacer(Modifier.height(12.dp))
            TextButton(onClick = onCancel, modifier = Modifier.fillMaxWidth()) { Text("Back to Login") }
        }
    }
}