package com.trustmarket.app.network

import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor : Interceptor {
    @Volatile
    var authToken: String? = null

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val requestBuilder = originalRequest.newBuilder()

        // Automatically attach JWT token to header if available
        authToken?.let { token ->
            if (token.isNotBlank()) {
                requestBuilder.header("Authorization", "Bearer $token")
            }
        }

        val response = chain.proceed(requestBuilder.build())

        // Handle global auth failures
        if (response.code == 401) {
            // Token expired or invalid
            SessionManager.onSessionExpired()
        }

        return response
    }
}

object SessionManager {
    var onSessionExpiredListener: (() -> Unit)? = null

    fun onSessionExpired() {
        onSessionExpiredListener?.invoke()
    }
}