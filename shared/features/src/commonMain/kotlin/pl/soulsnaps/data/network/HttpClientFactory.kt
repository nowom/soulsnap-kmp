package pl.soulsnaps.data.network

import io.ktor.client.*

/**
 * Platform-specific HttpClient factory
 * Based on Ktor best practices from: 
 * https://medium.com/appcent/kotlin-multiplatform-a-guide-to-ktor-integration-3c480c8ad545
 */
expect val httpClient: HttpClient


