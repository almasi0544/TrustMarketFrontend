package com.trustmarket.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import com.trustmarket.app.ui.screens.LoginScreen
import com.trustmarket.app.ui.screens.MarketplaceHomeScreen
import com.trustmarket.app.ui.screens.RegisterScreen
import com.trustmarket.app.ui.screens.TrustMarketHome
import com.trustmarket.app.ui.theme.TrustMarketTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TrustMarketTheme {
                AppRoot()
            }
        }
    }
}

@Composable
fun AppRoot() {
    var screen by remember { mutableStateOf("home") }
    var authToken by remember { mutableStateOf<String?>(null) }

    when (screen) {
        "home" -> TrustMarketHome(onGetStarted = { screen = "login" })
        "login" -> LoginScreen(
            onLoginSuccess = { token ->
                authToken = token
                screen = "marketplace"
            },
            onGoToRegister = { screen = "register" }
        )
        "register" -> RegisterScreen(
            onRegisterSuccess = { screen = "login" },
            onGoToLogin = { screen = "login" }
        )
        "marketplace" -> MarketplaceHomeScreen(token = authToken)
    }
}