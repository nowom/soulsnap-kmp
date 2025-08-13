package pl.soulsnaps.network

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.headers
import kotlinx.serialization.Serializable
import pl.soulsnaps.domain.model.*

@Serializable
private data class SupabaseResponse<T>(
    val data: T?,
    val error: SupabaseError?
)

@Serializable
private data class SupabaseError(
    val message: String,
    val details: String? = null,
    val hint: String? = null,
    val code: String? = null
)

class SupabaseDatabaseService(
    private val httpClient: HttpClient,
    private val supabaseUrl: String,
    private val supabaseAnonKey: String
) {
    
    // Memory operations
    suspend fun getAllMemories(): List<DatabaseMemory> {
        return try {
            val response = httpClient.get("$supabaseUrl/rest/v1/memories?select=*&order=created_at.desc") {
                contentType(ContentType.Application.Json)
                headers {
                    append("apikey", supabaseAnonKey)
                    append("Authorization", "Bearer $supabaseAnonKey")
                }
            }.body<Array<DatabaseMemory>>()
            
            response.toList()
        } catch (e: Exception) {
            println("Error fetching memories: ${e.message}")
            emptyList()
        }
    }
    
    suspend fun getMemoryById(id: String): DatabaseMemory? {
        return try {
            val response = httpClient.get("$supabaseUrl/rest/v1/memories?id=eq.$id&select=*") {
                contentType(ContentType.Application.Json)
                headers {
                    append("apikey", supabaseAnonKey)
                    append("Authorization", "Bearer $supabaseAnonKey")
                }
            }.body<Array<DatabaseMemory>>()
            
            response.firstOrNull()
        } catch (e: Exception) {
            println("Error fetching memory by ID: ${e.message}")
            null
        }
    }
    
    suspend fun createMemory(memory: CreateMemoryRequest): DatabaseMemory {
        return try {
            val response = httpClient.post("$supabaseUrl/rest/v1/memories") {
                contentType(ContentType.Application.Json)
                setBody(memory)
                headers {
                    append("apikey", supabaseAnonKey)
                    append("Authorization", "Bearer $supabaseAnonKey")
                    append("Prefer", "return=representation")
                }
            }.body<Array<DatabaseMemory>>()
            
            response.first()
        } catch (e: Exception) {
            println("Error creating memory: ${e.message}")
            throw e
        }
    }
    
    suspend fun updateMemory(id: String, memory: UpdateMemoryRequest): DatabaseMemory? {
        return try {
            val response = httpClient.patch("$supabaseUrl/rest/v1/memories?id=eq.$id") {
                contentType(ContentType.Application.Json)
                setBody(memory)
                headers {
                    append("apikey", supabaseAnonKey)
                    append("Authorization", "Bearer $supabaseAnonKey")
                    append("Prefer", "return=representation")
                }
            }.body<Array<DatabaseMemory>>()
            
            response.firstOrNull()
        } catch (e: Exception) {
            println("Error updating memory: ${e.message}")
            null
        }
    }
    
    suspend fun deleteMemory(id: String): Boolean {
        return try {
            val response = httpClient.delete("$supabaseUrl/rest/v1/memories?id=eq.$id") {
                contentType(ContentType.Application.Json)
                headers {
                    append("apikey", supabaseAnonKey)
                    append("Authorization", "Bearer $supabaseAnonKey")
                }
            }
            
            response.status.value in 200..299
        } catch (e: Exception) {
            println("Error deleting memory: ${e.message}")
            false
        }
    }
    
    suspend fun toggleMemoryFavorite(id: String, isFavorite: Boolean): DatabaseMemory? {
        return updateMemory(id, UpdateMemoryRequest(is_favorite = isFavorite))
    }
    
    // User profile operations
    suspend fun getUserProfile(): DatabaseUserProfile? {
        return try {
            val response = httpClient.get("$supabaseUrl/rest/v1/user_profiles?select=*") {
                contentType(ContentType.Application.Json)
                headers {
                    append("apikey", supabaseAnonKey)
                    append("Authorization", "Bearer $supabaseAnonKey")
                }
            }.body<Array<DatabaseUserProfile>>()
            
            response.firstOrNull()
        } catch (e: Exception) {
            println("Error fetching user profile: ${e.message}")
            null
        }
    }
    
    suspend fun createUserProfile(profile: CreateUserProfileRequest): DatabaseUserProfile {
        return try {
            val response = httpClient.post("$supabaseUrl/rest/v1/user_profiles") {
                contentType(ContentType.Application.Json)
                setBody(profile)
                headers {
                    append("apikey", supabaseAnonKey)
                    append("Authorization", "Bearer $supabaseAnonKey")
                    append("Prefer", "return=representation")
                }
            }.body<Array<DatabaseUserProfile>>()
            
            response.first()
        } catch (e: Exception) {
            println("Error creating user profile: ${e.message}")
            throw e
        }
    }
    
    suspend fun updateUserProfile(profile: UpdateUserProfileRequest): DatabaseUserProfile? {
        return try {
            val response = httpClient.patch("$supabaseUrl/rest/v1/user_profiles") {
                contentType(ContentType.Application.Json)
                setBody(profile)
                headers {
                    append("apikey", supabaseAnonKey)
                    append("Authorization", "Bearer $supabaseAnonKey")
                    append("Prefer", "return=representation")
                }
            }.body<Array<DatabaseUserProfile>>()
            
            response.firstOrNull()
        } catch (e: Exception) {
            println("Error updating user profile: ${e.message}")
            null
        }
    }
    
    // Search and filtering
    suspend fun searchMemories(query: String): List<DatabaseMemory> {
        return try {
            val response = httpClient.get("$supabaseUrl/rest/v1/memories?or=(title.ilike.%$query%,description.ilike.%$query%)&order=created_at.desc") {
                contentType(ContentType.Application.Json)
                headers {
                    append("apikey", supabaseAnonKey)
                    append("Authorization", "Bearer $supabaseAnonKey")
                }
            }.body<Array<DatabaseMemory>>()
            
            response.toList()
        } catch (e: Exception) {
            println("Error searching memories: ${e.message}")
            emptyList()
        }
    }
    
    suspend fun getMemoriesByMood(moodType: String): List<DatabaseMemory> {
        return try {
            val response = httpClient.get("$supabaseUrl/rest/v1/memories?mood_type=eq.$moodType&order=created_at.desc") {
                contentType(ContentType.Application.Json)
                headers {
                    append("apikey", supabaseAnonKey)
                    append("Authorization", "Bearer $supabaseAnonKey")
                }
            }.body<Array<DatabaseMemory>>()
            
            response.toList()
        } catch (e: Exception) {
            println("Error fetching memories by mood: ${e.message}")
            emptyList()
        }
    }
    
    suspend fun getFavoriteMemories(): List<DatabaseMemory> {
        return try {
            val response = httpClient.get("$supabaseUrl/rest/v1/memories?is_favorite=eq.true&order=created_at.desc") {
                contentType(ContentType.Application.Json)
                headers {
                    append("apikey", supabaseAnonKey)
                    append("Authorization", "Bearer $supabaseAnonKey")
                }
            }.body<Array<DatabaseMemory>>()
            
            response.toList()
        } catch (e: Exception) {
            println("Error fetching favorite memories: ${e.message}")
            emptyList()
        }
    }
}
