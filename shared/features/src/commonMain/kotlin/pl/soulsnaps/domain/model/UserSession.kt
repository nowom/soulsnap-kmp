package pl.soulsnaps.domain.model

data class UserSession(
    val userId: String,
    val email: String?,
    val isAnonymous: Boolean
)


