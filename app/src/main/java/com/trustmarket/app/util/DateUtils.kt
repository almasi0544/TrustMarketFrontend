package com.trustmarket.app.util

import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

fun daysAgo(isoDateTime: String): Int {
    return try {
        val then = OffsetDateTime.parse(isoDateTime)
        ChronoUnit.DAYS.between(then.toInstant(), java.time.Instant.now()).toInt()
    } catch (e: Exception) {
        0
    }
}

fun memberSince(isoDateTime: String): String {
    return try {
        OffsetDateTime.parse(isoDateTime).format(DateTimeFormatter.ofPattern("MMM yyyy"))
    } catch (e: Exception) {
        ""
    }
}

fun formatPrice(price: Double): String {
    return if (price == price.toLong().toDouble()) "$${price.toLong()}" else "$${"%.2f".format(price)}"
}