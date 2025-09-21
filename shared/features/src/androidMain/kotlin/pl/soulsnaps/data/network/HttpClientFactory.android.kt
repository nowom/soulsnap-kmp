package pl.soulsnaps.data.network

import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

/**
 * Android-specific HttpClient implementation using OkHttp engine
 * Based on Ktor best practices and optimized for Android platform
 */
actual val httpClient: HttpClient = HttpClient(OkHttp) {
    
    // Timeout configuration for Android
    install(HttpTimeout) {
        socketTimeoutMillis = 60_000
        requestTimeoutMillis = 60_000
        connectTimeoutMillis = 60_000
    }
    
    // Logging plugin for debugging
    install(Logging) {
        logger = Logger.DEFAULT
        level = LogLevel.INFO // Use INFO for production, ALL for debug
        logger = object : Logger {
            override fun log(message: String) {
                println("KtorClient-Android: $message")
            }
        }
    }
    
    // Content negotiation for JSON serialization
    install(ContentNegotiation) {
        json(Json {
            prettyPrint = true
            isLenient = true
            ignoreUnknownKeys = true
            explicitNulls = false
            useAlternativeNames = false
        })
    }
    
    // Default request configuration
    defaultRequest {
        headers.append("Content-Type", "application/json")
        headers.append("User-Agent", "SoulSnaps-Android/1.0")
    }
    
    // OkHttp engine specific configuration
    engine {
        config {
            // Retry configuration
            retryOnConnectionFailure(true)
        }
        
        // Custom interceptors can be added here
        // addInterceptor { chain ->
        //     val request = chain.request()
        //     // Custom request modification
        //     chain.proceed(request)
        // }
    }
}
