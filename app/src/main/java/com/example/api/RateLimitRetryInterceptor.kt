package com.example.api

import android.util.Log
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException
import kotlin.math.min
import kotlin.math.pow
import kotlin.random.Random

/**
 * OkHttp Interceptor that detects HTTP 429 (Too Many Requests) as well as 503/529 errors
 * and applies exponential backoff with full randomized jitter to gracefully handle rate limits.
 */
class RateLimitRetryInterceptor(
    private val maxRetries: Int = 3,
    private val initialDelayMs: Long = 1000L,
    private val maxDelayMs: Long = 8000L
) : Interceptor {

    companion object {
        private const val TAG = "RateLimitRetry"
        private const val HTTP_TOO_MANY_REQUESTS = 429
        private const val HTTP_SERVICE_UNAVAILABLE = 503
        private const val HTTP_SITE_OVERLOADED = 529
    }

    private fun logWarn(msg: String) {
        try {
            android.util.Log.w(TAG, msg)
        } catch (_: Throwable) {
            println("[$TAG] $msg")
        }
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        var response: Response? = null
        var attempt = 0

        while (attempt <= maxRetries) {
            try {
                response = chain.proceed(request)

                if (response.code == HTTP_TOO_MANY_REQUESTS ||
                    response.code == HTTP_SERVICE_UNAVAILABLE ||
                    response.code == HTTP_SITE_OVERLOADED
                ) {
                    if (attempt >= maxRetries) {
                        logWarn("Exhausted $maxRetries retries for HTTP ${response.code}")
                        return response
                    }

                    // Check server Retry-After header (in seconds)
                    val retryAfterHeader = response.header("Retry-After")
                    val retryAfterSeconds = retryAfterHeader?.toLongOrNull()
                    val baseDelay = if (retryAfterSeconds != null && retryAfterSeconds > 0) {
                        retryAfterSeconds * 1000L
                    } else {
                        val exponential = initialDelayMs * (2.0.pow(attempt.toDouble())).toLong()
                        min(exponential, maxDelayMs)
                    }

                    // Full jitter: decorrelated random interval to avoid thundering herd synchronization
                    val jitter = if (baseDelay > 100) Random.nextLong(0, baseDelay / 2) else Random.nextLong(50, 200)
                    val totalDelay = baseDelay + jitter

                    logWarn("Received HTTP ${response.code} (attempt ${attempt + 1}/$maxRetries). Backing off for ${totalDelay}ms with jitter...")

                    // Safely close previous response body to prevent socket/connection resource leaks
                    response.close()

                    try {
                        Thread.sleep(totalDelay)
                    } catch (ie: InterruptedException) {
                        Thread.currentThread().interrupt()
                        throw IOException("Retry interrupted", ie)
                    }

                    attempt++
                } else {
                    return response
                }
            } catch (e: IOException) {
                if (attempt >= maxRetries) {
                    throw e
                }
                logWarn("Transient network exception on attempt $attempt: ${e.message}. Retrying...")
                val jitter = Random.nextLong(200, 600)
                try {
                    Thread.sleep(initialDelayMs + jitter)
                } catch (ie: InterruptedException) {
                    Thread.currentThread().interrupt()
                    throw IOException("Retry interrupted", ie)
                }
                attempt++
            }
        }

        return response ?: chain.proceed(request)
    }
}
