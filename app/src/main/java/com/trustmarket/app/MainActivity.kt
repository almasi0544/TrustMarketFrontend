package com.trustmarket.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import com.trustmarket.app.ui.screens.*
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
    var backStack by remember { mutableStateOf(listOf("home")) }
    val screen = backStack.last()

    fun navigate(to: String) { backStack = backStack + to }
    fun goBack() { if (backStack.size > 1) backStack = backStack.dropLast(1) }
    fun resetTo(newRoot: String) { backStack = listOf(newRoot) }
    fun replaceLastTwoWith(target: String) {
        backStack = backStack.dropLast(2).ifEmpty { listOf("marketplace") } + target
    }

    BackHandler(enabled = backStack.size > 1) { goBack() }

    var authToken by remember { mutableStateOf<String?>(null) }
    var selectedSellerId by remember { mutableStateOf<Int?>(null) }
    var selectedTransactionId by remember { mutableStateOf<Int?>(null) }
    var selectedProductId by remember { mutableStateOf<Int?>(null) }
    var editingProductId by remember { mutableStateOf<Int?>(null) }

    when (screen) {
        "home" -> TrustMarketHome(onGetStarted = { navigate("login") })
        "login" -> LoginScreen(
            onLoginSuccess = { token -> authToken = token; resetTo("marketplace") },
            onGoToRegister = { navigate("register") },
            onForgotPassword = { navigate("forgot_password") }
        )

        "forgot_password" -> ForgotPasswordScreen(
            onCodeSent = { navigate("reset_password") },
            onCancel = { goBack() }
        )

        "reset_password" -> ResetPasswordScreen(
            onResetSuccess = { resetTo("login") },
            onCancel = { goBack() }
        )

        "register" -> RegisterScreen(
            onRegisterSuccess = { goBack() },
            onGoToLogin = { goBack() }
        )

        "profile" -> ProfileScreen(
            token = authToken,
            onEdit = { navigate("edit_profile") },
            onViewDisputes = { navigate("my_disputes") },
            onViewVerification = { navigate("verification") },
            onViewTrustProfile = { sellerId -> selectedSellerId = sellerId; navigate("trust_profile") },
            onSignOut = { authToken = null; resetTo("login") }
        )

        "edit_profile" -> EditProfileScreen(token = authToken, onSaved = { goBack() })

        "marketplace" -> MarketplaceHomeScreen(
            token = authToken,
            onNavigateToProfile = { navigate("profile") },
            onViewProduct = { productId -> selectedProductId = productId; navigate("product_detail") },
            onNavigateToPurchases = { navigate("my_purchases") },
            onNavigateToMyProducts = { navigate("my_products") },
            onNavigateToDisputes = { navigate("my_disputes") }
        )

        "product_detail" -> {
            val id = selectedProductId
            if (id != null) {
                ProductDetailScreen(
                    productId = id,
                    onBack = { goBack() },
                    onBuy = { pid -> selectedProductId = pid; navigate("buy_confirm") },
                    onReportSeller = { sellerId -> selectedSellerId = sellerId; navigate("report_seller") },
                    onViewTrustProfile = { sellerId -> selectedSellerId = sellerId; navigate("trust_profile") }
                )
            }
        }

        "buy_confirm" -> {
            val id = selectedProductId
            if (id != null) {
                BuyConfirmationScreen(
                    token = authToken,
                    productId = id,
                    onConfirmed = { txnId, paymentMethod ->
                        selectedTransactionId = txnId
                        if (paymentMethod == "mobile_money") {
                            replaceLastTwoWith("mobile_money_payment")
                        } else {
                            replaceLastTwoWith("my_purchases")
                        }
                    },
                    onCancel = { goBack() }
                )
            }
        }

        "mobile_money_payment" -> {
            val id = selectedTransactionId
            if (id != null) {
                MobileMoneyPaymentScreen(
                    token = authToken,
                    transactionId = id,
                    onInitiated = { navigate("payment_pending") },
                    onCancel = { resetTo("marketplace") }
                )
            }
        }

        "payment_pending" -> {
            val id = selectedTransactionId
            if (id != null) {
                PaymentPendingScreen(
                    token = authToken,
                    transactionId = id,
                    onDone = { resetTo("marketplace"); navigate("transaction_detail") }
                )
            }
        }

        "my_purchases" -> MyPurchasesScreen(
            token = authToken,
            onOpenTransaction = { txnId -> selectedTransactionId = txnId; navigate("transaction_detail") }
        )

        "transaction_detail" -> {
            val id = selectedTransactionId
            if (id != null) {
                TransactionDetailScreen(
                    token = authToken,
                    transactionId = id,
                    onBack = { goBack() },
                    onRaiseDispute = { txnId -> selectedTransactionId = txnId; navigate("create_dispute") },
                    onReportSeller = { sellerId -> selectedSellerId = sellerId; navigate("report_seller") },
                    onViewTrustProfile = { sellerId -> selectedSellerId = sellerId; navigate("trust_profile") }
                )
            }
        }

        "my_products" -> MyProductsScreen(
            token = authToken,
            onCreateProduct = { editingProductId = null; navigate("product_form") },
            onEditProduct = { productId -> editingProductId = productId; navigate("product_form") }
        )

        "product_form" -> ProductFormScreen(
            token = authToken,
            productId = editingProductId,
            onSaved = { goBack() },
            onCancel = { goBack() }
        )

        "trust_profile" -> {
            val id = selectedSellerId
            if (id != null) {
                TrustProfileScreen(
                    sellerId = id,
                    onBack = { goBack() },
                    onReportSeller = { navigate("report_seller") }
                )
            }
        }

        "report_seller" -> {
            val id = selectedSellerId
            if (id != null) {
                ReportSellerScreen(
                    token = authToken,
                    sellerId = id,
                    onSubmitted = { goBack() },
                    onCancel = { goBack() }
                )
            }
        }

        "create_dispute" -> {
            val id = selectedTransactionId
            if (id != null) {
                CreateDisputeScreen(
                    token = authToken,
                    transactionId = id,
                    onSubmitted = { replaceLastTwoWith("my_disputes") },
                    onCancel = { goBack() }
                )
            }
        }

        "my_disputes" -> MyDisputesScreen(token = authToken, onBack = { goBack() })

        "verification" -> VerificationScreen(token = authToken, onBack = { goBack() })
    }
}