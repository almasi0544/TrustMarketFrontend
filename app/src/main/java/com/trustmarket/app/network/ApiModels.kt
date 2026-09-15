package com.trustmarket.app.network

data class RegisterRequest(
    val email: String,
    val password: String
)

data class LoginRequest(
    val email: String,
    val password: String
)

data class UserOut(
    val id: Int,
    val email: String
)

data class TokenResponse(
    val access_token: String,
    val token_type: String
)

data class ProductOut(
    val id: Int,
    val seller_id: Int,
    val category_id: Int,
    val title: String,
    val description: String?,
    val price: Double,
    val condition: String,
    val status: String
)

data class CategoryOut(val id: Int, val name: String, val description: String?)

data class TrustProfileOut(
    val seller_id: Int,
    val email: String,
    val account_age_days: Int,
    val total_transactions: Int,
    val completed_transactions: Int,
    val cancelled_transactions: Int,
    val completion_rate: Double,
    val trust_score: Int,
    val risk_level: String
)