package com.trustmarket.app.network

import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.PUT
import retrofit2.http.Header

interface ApiService {
    @POST("api/v1/auth/register")
    suspend fun register(@Body request: RegisterRequest): UserOut

    @FormUrlEncoded
    @POST("api/v1/auth/login")
    suspend fun login(
        @Field("username") username: String,
        @Field("password") password: String
    ): TokenResponse

    @GET("api/v1/products")
    suspend fun getProducts(): List<ProductOut>

    @GET("api/v1/categories")
    suspend fun getCategories(): List<CategoryOut>

    @GET("api/v1/sellers/{sellerId}/trust")
    suspend fun getSellerTrust(@Path("sellerId") sellerId: Int): TrustProfileOut

    @GET("api/v1/users/me/profile")
    suspend fun getMyProfile(@Header("Authorization") token: String): ProfileOut

    @PUT("api/v1/users/me/profile")
    suspend fun updateMyProfile(@Header("Authorization") token: String, @Body request: ProfileUpdateRequest): ProfileOut

    @POST("api/v1/reports")
    suspend fun reportSeller(@Header("Authorization") token: String, @Body request: ReportRequest): Any

    @POST("api/v1/disputes")
    suspend fun createDispute(@Header("Authorization") token: String, @Body request: DisputeRequest): DisputeOut

    @GET("api/v1/disputes/mine")
    suspend fun getMyDisputes(@Header("Authorization") token: String): List<DisputeOut>

    @GET("api/v1/verification/me")
    suspend fun getMyVerification(@Header("Authorization") token: String): VerificationOut

    @POST("api/v1/verification/me/verify-email")
    suspend fun verifyMyEmail(@Header("Authorization") token: String): VerificationOut
}