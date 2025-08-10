package pl.soulsnaps.network

import io.ktor.client.HttpClient

expect class HttpClientFactory() {
    fun create(): HttpClient
}


