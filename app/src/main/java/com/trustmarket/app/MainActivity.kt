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
import com.trustmarket.app.ui.screens.ProfileScreen
import com.trustmarket.app.ui.screens.EditProfileScreen
import com.trustmarket.app.ui.screens.TrustProfileScreen
import com.trustmarket.app.ui.screens.ReportSellerScreen
import com.trustmarket.app.ui.screens.CreateDisputeScreen
import com.trustmarket.app.ui.screens.MyDisputesScreen
import com.trustmarket.app.ui.screens.VerificationScreen

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
    var selectedSellerId by remember { mutableStateOf<Int?>(null) }
    var selectedTransactionId by remember { mutableStateOf<Int?>(null) }

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
        "profile" -> ProfileScreen(
            token = authToken,
            onEdit = { screen = "edit_profile" },
            onViewDisputes = { screen = "my_disputes" },
            onViewVerification = { screen = "verification" }
        )
        "edit_profile" -> EditProfileScreen(token = authToken, onSaved = { screen = "profile" })
        "marketplace" -> MarketplaceHomeScreen(
            token = authToken,
            onNavigateToProfile = { screen = "profile" },
            onViewTrustProfile = { sellerId -> selectedSellerId = sellerId; screen = "trust_profile" }

        )
        "my_disputes" -> MyDisputesScreen(token = authToken, onBack = { screen = "marketplace" })
        "verification" -> VerificationScreen(token = authToken, onBack = { screen = "profile" })
        "report_seller" -> {
            val id = selectedSellerId
            if (id != null) {
                ReportSellerScreen(
                    token = authToken,
                    sellerId = id,
                    onSubmitted = { screen = "trust_profile" },
                    onCancel = { screen = "trust_profile" }
                )
            }
        }
        "create_dispute" -> {
            val id = selectedTransactionId
            if (id != null) {
                CreateDisputeScreen(
                    token = authToken,
                    transactionId = id,
                    onSubmitted = { screen = "marketplace" },
                    onCancel = { screen = "marketplace" }
                )
            }
        }
        "trust_profile" -> {
            val id = selectedSellerId
            if (id != null) {
                TrustProfileScreen(
                    sellerId = id,
                    onBack = { screen = "marketplace" },
                    onReportSeller = { screen = "report_seller" }
                )
            }
        }
    }
}