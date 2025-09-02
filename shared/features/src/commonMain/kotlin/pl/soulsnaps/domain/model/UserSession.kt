package pl.soulsnaps.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class UserSession(
    val userId: String,
    val email: String,
    val isAnonymous: Boolean = false,
    val displayName: String? = null,
    val createdAt: Long,
    val lastActiveAt: Long,
    val accessToken: String? = null,
    val refreshToken: String? = null
)


