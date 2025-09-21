package pl.soulsnaps.data.network

import io.ktor.client.*
import io.ktor.client.engine.darwin.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

/**
 * iOS-specific HttpClient implementation using Darwin engine
 * Based on Ktor best practices and optimized for iOS platform
 */
actual val httpClient: HttpClient = HttpClient(Darwin) {
    
    // Timeout configuration for iOS
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
                println("KtorClient-iOS: $message")
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
        headers.append("User-Agent", "SoulSnaps-iOS/1.0")
    }
    
    // Darwin engine specific configuration
    engine {
        configureRequest {
            // iOS-specific request configuration
            setAllowsCellularAccess(true)
            setAllowsConstrainedNetworkAccess(true)
            setAllowsExpensiveNetworkAccess(true)
        }
        
        configureSession {
            // URLSession configuration for iOS
            sessionSendsLaunchEvents = true
            discretionary = false
        }
    }
}
