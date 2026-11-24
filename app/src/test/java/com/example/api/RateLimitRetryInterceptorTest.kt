package com.example.api

import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.concurrent.atomic.AtomicInteger

class RateLimitRetryInterceptorTest {

    @Test
    fun `test retry on 429 retries and succeeds`() {
        val attempts = AtomicInteger(0)
        val request = Request.Builder()
            .url("https://generativelanguage.googleapis.com/test")
            .build()

        val interceptor = RateLimitRetryInterceptor(
            maxRetries = 2,
            initialDelayMs = 10L,
            maxDelayMs = 50L
        )

        val chain = object : Interceptor.Chain {
            override fun request(): Request = request
            override fun proceed(request: Request): Response {
                val count = attempts.incrementAndGet()
                return if (count < 2) {
                    Response.Builder()
                        .request(request)
                        .protocol(Protocol.HTTP_1_1)
                        .code(429)
                        .message("Too Many Requests")
                        .body("Rate limit reached".toResponseBody("application/json".toMediaType()))
                        .build()
                } else {
                    Response.Builder()
                        .request(request)
                        .protocol(Protocol.HTTP_1_1)
                        .code(200)
                        .message("OK")
                        .body("{\"status\":\"success\"}".toResponseBody("application/json".toMediaType()))
                        .build()
                }
            }

            override fun connection(): okhttp3.Connection? = null
            override fun call(): okhttp3.Call = throw UnsupportedOperationException()
            override fun connectTimeoutMillis(): Int = 1000
            override fun withConnectTimeout(timeout: Int, unit: java.util.concurrent.TimeUnit): Interceptor.Chain = this
            override fun readTimeoutMillis(): Int = 1000
            override fun withReadTimeout(timeout: Int, unit: java.util.concurrent.TimeUnit): Interceptor.Chain = this
            override fun writeTimeoutMillis(): Int = 1000
            override fun withWriteTimeout(timeout: Int, unit: java.util.concurrent.TimeUnit): Interceptor.Chain = this
        }

        val finalResponse = interceptor.intercept(chain)
        assertEquals(200, finalResponse.code)
        assertEquals(2, attempts.get())
    }
}
