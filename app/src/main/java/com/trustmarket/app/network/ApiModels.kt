package com.trustmarket.app.network

// ==========================================
// User & Auth Models
// ==========================================

data class RegisterRequest(
    val email: String,
    val password: String
)

data class LoginRequest(
    val email: String,
    val password: String
)

data class TokenResponse(
    val access_token: String,
    val token_type: String
)

data class UserOut(
    val id: Int,
    val email: String,
    val full_name: String? = null,
    val is_active: Boolean = true,
    val created_at: String? = null
)

data class ProfileOut(
    val user_id: Int,
    val full_name: String? = null,
    val username: String? = null,
    val bio: String? = null,
    val location: String? = null,
    val profile_image: String? = null
)

data class ProfileUpdateRequest(
    val full_name: String? = null,
    val username: String? = null,
    val bio: String? = null,
    val location: String? = null
)

// ==========================================
// Product Models
// ==========================================

data class CategoryOut(
    val id: Int,
    val name: String,
    val description: String? = null
)

data class ProductOut(
    val id: Int,
    val title: String,
    val description: String? = null,
    val price: Double,
    val category_id: Int? = null,
    val seller_id: Int,
    val condition: String? = null,
    val status: String? = null,
    val is_active: Boolean = true,
    val created_at: String? = null,
    val shipping_cost: Double,
    val media_url: String? = null,
    val media_type: String? = null
)
data class ProductCreateRequest(
    val title: String,
    val description: String?,
    val price: Double,
    val condition: String,
    val category_id: Int,
    val media_url: String? = null,
    val media_type: String? = null,
    val shipping_cost: Double = 0.0
)

// ==========================================
// Transaction Models
// ==========================================
data class TransactionCreateRequest(val product_id: Int, val payment_method: String)

data class ShippingUpdateRequest(val tracking_reference: String, val expected_delivery_date: String)

data class ForgotPasswordRequest(val email: String)
data class ResetPasswordRequest(val token: String, val new_password: String)

data class TransactionOut(
    val id: Int,
    val buyer_id: Int,
    val seller_id: Int,
    val product_id: Int,
    val amount: Double,
    val payment_method: String,
    val buyer_protection_fee: Double,
    val shipping_cost: Double,
    val tracking_reference: String?,
    val expected_delivery_date: String?,
    val status: String,
    val created_at: String,
    val completed_at: String?
)

// ==========================================
// Dispute & Report Models
// ==========================================

data class DisputeRequest(
    val transaction_id: Int,
    val reason: String
)

data class DisputeCreateRequest(
    val transaction_id: Int,
    val reason: String,
    val description: String
)

data class DisputeOut(
    val id: Int,
    val transaction_id: Int,
    val reporter_id: Int? = null,
    val raised_by_id: Int? = null,
    val reason: String,
    val description: String? = null,
    val status: String,
    val resolution_notes: String? = null,
    val created_at: String,
    val resolved_at: String? = null
)

data class ReportRequest(
    val seller_id: Int,
    val transaction_id: Int? = null,
    val category: String,
    val description: String? = null,
    val evidence_url: String,
    val evidence_type: String
)

// ==========================================
// Trust, Verification & Stats Models
// ==========================================

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

data class VerificationOut(
    val user_id: Int,
    val level: String
)

data class VerificationStatusOut(
    val level: String,
    val email_verified: Boolean,
    val phone_verified: Boolean,
    val identity_verified: Boolean,
    val business_verified: Boolean
)

data class SellerStatsOut(
    val seller_id: Int,
    val email: String,
    val total_products: Int,
    val active_products: Int
)

// ==========================================
// Review Models
// ==========================================

data class ReviewCreateRequest(
    val transaction_id: Int,
    val rating: Int,
    val comment: String
)

data class ReviewOut(
    val id: Int,
    val transaction_id: Int,
    val reviewer_id: Int,
    val seller_id: Int,
    val rating: Int,
    val comment: String,
    val created_at: String
)

data class ProductUpdateRequest(
    val title: String? = null,
    val description: String? = null,
    val price: Double? = null,
    val condition: String? = null,
    val category_id: Int? = null,
    val shipping_cost: Double? = null,
    val media_url: String? = null,
    val media_type: String? = null
)

data class MessageResponse(val detail: String)

data class MobileMoneyPaymentRequest(val phone_number: String)

data class MobileMoneyPaymentResponse(
    val detail: String,
    val provider_response: Map<String, Any>? = null
)

data class PaymentStatusOut(
    val payment_status: String?,
    val zenopay_order_id: String?
)


data class UploadResponse(val media_url: String, val media_type: String)