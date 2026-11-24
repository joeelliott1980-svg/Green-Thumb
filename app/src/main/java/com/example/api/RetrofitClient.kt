package com.example.api

/**
 * Unified RetrofitClient referencing GeminiClient to maintain a single connection pool,
 * shared concurrency throttling, and exponential backoff retry mechanism.
 */
object RetrofitClient {
    val service: GeminiApiService
        get() = GeminiClient.service

    val apiKey: String
        get() = GeminiClient.apiKey
}
