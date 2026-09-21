package com.trustmarket.app.network

import retrofit2.http.*

interface ApiService {

    // ==========================================
    // Auth
    // ==========================================

    @POST("api/v1/auth/register")
    suspend fun register(
        @Body request: RegisterRequest
    ): UserOut

    @FormUrlEncoded
    @POST("api/v1/auth/login")
    suspend fun login(
        @Field("username") username: String,
        @Field("password") password: String
    ): TokenResponse

    // ==========================================
    // User Profile & Verification
    // ==========================================

    @GET("api/v1/users/me/profile")
    suspend fun getMyProfile(
        @Header("Authorization") token: String? = null
    ): ProfileOut

    @PUT("api/v1/users/me/profile")
    suspend fun updateMyProfile(
        @Body request: ProfileUpdateRequest,
        @Header("Authorization") token: String? = null
    ): ProfileOut

    @GET("api/v1/users/me/verification")
    suspend fun getMyVerification(
        @Header("Authorization") token: String? = null
    ): VerificationStatusOut

    @POST("api/v1/users/me/verification/verify-email")
    suspend fun verifyEmail(
        @Header("Authorization") token: String? = null
    ): VerificationStatusOut

    // ==========================================
    // Products & Categories
    // ==========================================

    @GET("api/v1/categories")
    suspend fun getCategories(): List<CategoryOut>

    @GET("api/v1/products")
    suspend fun getProducts(): List<ProductOut>

    @GET("api/v1/products/{id}")
    suspend fun getProduct(
        @Path("id") id: Int
    ): ProductOut

    @POST("api/v1/products")
    suspend fun createProduct(
        @Body request: ProductCreateRequest,
        @Header("Authorization") token: String? = null
    ): ProductOut

    @GET("api/v1/products/mine")
    suspend fun getMyProducts(
        @Header("Authorization") token: String? = null
    ): List<ProductOut>

    @PUT("api/v1/products/{id}")
    suspend fun updateProduct(
        @Path("id") id: Int,
        @Body request: ProductUpdateRequest,
        @Header("Authorization") token: String? = null
    ): ProductOut

    @DELETE("api/v1/products/{id}")
    suspend fun deleteProduct(
        @Header("Authorization") token: String? = null,
        @Path("id") id: Int
    ): Any

    // ==========================================
    // Sellers & Trust Metrics
    // ==========================================

    @GET("api/v1/sellers/{sellerId}/trust")
    suspend fun getSellerTrust(
        @Path("sellerId") sellerId: Int
    ): TrustProfileOut

    @GET("api/v1/sellers/{sellerId}")
    suspend fun getSellerStats(
        @Path("sellerId") sellerId: Int
    ): SellerStatsOut

    @POST("api/v1/reports")
    suspend fun reportSeller(
        @Body request: ReportRequest,
        @Header("Authorization") token: String? = null
    ): Any

    // ==========================================
    // Transactions
    // ==========================================

    @POST("api/v1/transactions")
    suspend fun createTransaction(
        @Body request: TransactionCreateRequest,
        @Header("Authorization") token: String? = null
    ): TransactionOut

    @GET("api/v1/transactions/mine")
    suspend fun getMyTransactions(
        @Header("Authorization") token: String? = null,
        @Query("role") role: String? = null
    ): List<TransactionOut>

    @GET("api/v1/transactions/{id}")
    suspend fun getTransaction(
        @Header("Authorization") token: String? = null,
        @Path("id") id: Int
    ): TransactionOut


    @POST("api/v1/transactions/{id}/complete")
    suspend fun completeTransaction(
        @Path("id") transactionId: Int,
        @Header("Authorization") token: String? = null
    ): TransactionOut

    // ==========================================
    // Disputes
    // ==========================================

    @POST("api/v1/disputes")
    suspend fun createDispute(
        @Body request: DisputeCreateRequest,
        @Header("Authorization") token: String? = null
    ): DisputeOut

    @GET("api/v1/disputes/mine")
    suspend fun getMyDisputes(
        @Header("Authorization") token: String? = null
    ): List<DisputeOut>

    // ==========================================
    // Reviews
    // ==========================================

    @POST("api/v1/reviews")
    suspend fun createReview(
        @Body request: ReviewCreateRequest,
        @Header("Authorization") token: String? = null
    ): ReviewOut

    @GET("api/v1/sellers/{sellerId}/reviews")
    suspend fun getSellerReviews(
        @Path("sellerId") sellerId: Int
    ): List<ReviewOut>

    @GET("api/v1/auth/me")
    suspend fun getCurrentUser(@Header("Authorization") token: String): UserOut
}