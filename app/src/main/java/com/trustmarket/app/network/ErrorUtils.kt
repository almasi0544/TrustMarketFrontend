package com.trustmarket.app.network

import org.json.JSONObject
import retrofit2.HttpException
import java.io.IOException

fun friendlyErrorMessage(e: Throwable): String {
    return when (e) {
        is HttpException -> {
            val backendDetail = try {
                val body = e.response()?.errorBody()?.string()
                if (!body.isNullOrBlank()) JSONObject(body).optString("detail", "") else ""
            } catch (parseError: Exception) {
                ""
            }
            if (backendDetail.isNotBlank()) {
                backendDetail
            } else {
                when (e.code()) {
                    400 -> "Please check your input and try again."
                    401 -> "Invalid email or password."
                    403 -> "You don't have permission to do that."
                    404 -> "Not found."
                    409 -> "This already exists."
                    422 -> "Please check that all fields are filled in correctly."
                    in 500..599 -> "Something went wrong on our end. Please try again."
                    else -> "Something went wrong."
                }
            }
        }
        is IOException -> "Could not connect to the server. Check your connection and try again."
        else -> "Something went wrong. Please try again."
    }
}