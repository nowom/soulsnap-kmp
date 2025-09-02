package pl.soulsnaps.features.auth.model

import pl.soulsnaps.utils.getCurrentTimeMillis

/**
 * Missing Models - Classes that were referenced but not defined
 * These are needed to resolve compilation errors
 */

/**
 * Memory Insight - Insight generated from memory analysis
 */
data class MemoryInsight(
    val id: String,
    val memoryId: String,
    val insightType: String,
    val content: String,
    val confidence: Float,
    val generatedAt: Long = getCurrentTimeMillis()
)

/**
 * Analysis Capabilities - What analysis features are available
 */
data class AnalysisCapabilities(
    val canAnalyzePhotos: Boolean,
    val canAnalyzeVideos: Boolean,
    val canAnalyzeAudio: Boolean,
    val canDetectPatterns: Boolean,
    val canGenerateInsights: Boolean,
    val maxAnalysisPerDay: Int,
    val supportedFormats: List<String>
)

/**
 * Plan Info - Information about a subscription plan
 */
data class PlanInfo(
    val planName: String,
    val displayName: String,
    val description: String,
    val monthlyPrice: Double?,
    val yearlyPrice: Double?,
    val features: List<String>,
    val limits: Map<String, Int>
)

/**
 * Plan Metadata - Additional information about plans
 */
data class PlanMetadata(
    val version: String,
    val lastUpdated: Long,
    val environment: String,
    val features: Map<String, Boolean>
)

/**
 * Config Service - Service for managing configuration
 */
interface ConfigService {
    fun getValue(key: String): String?
    fun getBoolean(key: String): Boolean?
    fun getInt(key: String): Int?
    fun setValue(key: String, value: String): Boolean
}

/**
 * Database Connection - Database connection interface
 */
interface DatabaseConnection {
    fun connect(): Boolean
    fun disconnect(): Boolean
    fun isConnected(): Boolean
}

/**
 * Redis Connection - Redis connection interface
 */
interface RedisConnection {
    fun connect(): Boolean
    fun disconnect(): Boolean
    fun isConnected(): Boolean
    fun get(key: String): String?
    fun set(key: String, value: String, ttl: Long? = null): Boolean
}

/**
 * Redis Cache - Redis cache implementation
 */
class RedisCache(
    private val connection: RedisConnection
) {
    fun get(key: String): String? = connection.get(key)
    fun set(key: String, value: String, ttl: Long? = null): Boolean = connection.set(key, value, ttl)
}

/**
 * Rate Limits - Rate limiting configuration
 */
data class RateLimits(
    val requestsPerMinute: Int,
    val requestsPerHour: Int,
    val requestsPerDay: Int,
    val burstLimit: Int
)

/**
 * Duration - Time duration utilities
 */
object Duration {
    const val MINUTE = 60L
    const val HOUR = 60L * MINUTE
    const val DAY = 24L * HOUR
    const val WEEK = 7L * DAY
    const val MONTH = 30L * DAY
    const val YEAR = 365L * DAY
}
