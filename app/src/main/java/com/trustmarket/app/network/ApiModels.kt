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
    val disputes_raised: Int,
    val disputes_resolved: Int,
    val reports_count: Int,
    val verification_level: String,
    val trust_score: Int,
    val risk_level: String
)

data class ProfileOut(
    val user_id: Int,
    val full_name: String?,
    val username: String?,
    val bio: String?,
    val location: String?,
    val profile_image: String?
)

data class ProfileUpdateRequest(
    val full_name: String? = null,
    val username: String? = null,
    val bio: String? = null,
    val location: String? = null
)

data class ReportRequest(
    val seller_id: Int,
    val transaction_id: Int? = null,
    val category: String,
    val description: String? = null
)

data class DisputeRequest(
    val transaction_id: Int,
    val reason: String
)

data class DisputeOut(
    val id: Int,
    val transaction_id: Int,
    val raised_by_id: Int,
    val reason: String,
    val status: String,
    val resolution_notes: String?,
    val created_at: String,
    val resolved_at: String?
)

data class VerificationOut(
    val user_id: Int,
    val level: String
)