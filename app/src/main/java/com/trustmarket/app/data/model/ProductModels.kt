package com.trustmarket.data.model

import com.google.gson.annotations.SerializedName

data class ProductCreateRequest(
    @SerializedName("title") val title: String,
    @SerializedName("description") val description: String,
    @SerializedName("price") val price: Double,
    @SerializedName("condition") val condition: String,
    @SerializedName("category_id") val categoryId: Int,
    @SerializedName("shipping_cost") val shippingCost: Double,
    @SerializedName("media_url") val mediaUrl: String?,
    @SerializedName("media_type") val mediaType: String?
)

data class ProductUpdateRequest(
    @SerializedName("title") val title: String,
    @SerializedName("description") val description: String,
    @SerializedName("price") val price: Double,
    @SerializedName("condition") val condition: String,
    @SerializedName("category_id") val categoryId: Int,
    @SerializedName("shipping_cost") val shippingCost: Double,
    @SerializedName("media_url") val mediaUrl: String?,
    @SerializedName("media_type") val mediaType: String?
)

data class ProductResponse(
    @SerializedName("id") val id: Int,
    @SerializedName("title") val title: String,
    @SerializedName("description") val description: String?,
    @SerializedName("price") val price: Double,
    @SerializedName("condition") val condition: String?,
    @SerializedName("seller_id") val sellerId: Int,
    @SerializedName("status") val status: String,
    @SerializedName("media_url") val mediaUrl: String?,
    @SerializedName("media_type") val mediaType: String?
)