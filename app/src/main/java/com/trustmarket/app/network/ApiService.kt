package com.trustmarket.app.network

import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST
import retrofit2.http.GET
import retrofit2.http.Path

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
}