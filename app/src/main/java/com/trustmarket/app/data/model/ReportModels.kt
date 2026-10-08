package com.trustmarket.data.model

import com.google.gson.annotations.SerializedName

data class ReportRequest(
    @SerializedName("seller_id") val sellerId: Int,
    @SerializedName("category") val category: String,
    @SerializedName("description") val description: String,
    @SerializedName("evidence_url") val evidenceUrl: String?,
    @SerializedName("evidence_type") val evidenceType: String?
)

data class ReportResponse(
    @SerializedName("id") val id: Int,
    @SerializedName("reporter_id") val reporterId: Int,
    @SerializedName("reported_seller_id") val reportedSellerId: Int,
    @SerializedName("reason") val reason: String,
    @SerializedName("description") val description: String?,
    @SerializedName("evidence_url") val evidenceUrl: String?,
    @SerializedName("evidence_type") val evidenceType: String?,
    @SerializedName("created_at") val createdAt: String
)