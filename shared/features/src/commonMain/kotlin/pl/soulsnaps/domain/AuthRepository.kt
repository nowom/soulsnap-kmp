package pl.soulsnaps.domain

import pl.soulsnaps.domain.model.UserSession

interface AuthRepository {
    suspend fun signIn(email: String, password: String): UserSession
    suspend fun register(email: String, password: String): UserSession
    suspend fun signInAnonymously(): UserSession
    fun signOut()
    fun currentUser(): UserSession?
}


