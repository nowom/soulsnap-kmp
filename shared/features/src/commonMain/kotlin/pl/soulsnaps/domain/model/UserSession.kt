package pl.soulsnaps.domain.model

data class UserSession(
    val userId: String,
    val email: String?,
    val isAnonymous: Boolean,
    val displayName: String? = null,
    val createdAt: Long = kotlinx.datetime.Clock.System.now().toEpochMilliseconds(),
    val lastActiveAt: Long = kotlinx.datetime.Clock.System.now().toEpochMilliseconds()
)


