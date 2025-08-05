package pl.soulsnaps.features.onboarding

import kotlinx.coroutines.flow.Flow

// For MVP, we'll use a simple in-memory implementation
// In production, you'd inject the context and use AndroidOnboardingDataStore(context)
actual fun createOnboardingDataStore(): OnboardingDataStore {
    return InMemoryOnboardingDataStore()
}

// Simple in-memory implementation for MVP
private class InMemoryOnboardingDataStore : OnboardingDataStore {
    private var isCompleted = false

    override val onboardingCompleted: Flow<Boolean> = kotlinx.coroutines.flow.flowOf(isCompleted)

    
    override suspend fun clearOnboardingData() {
        isCompleted = false
        println("Android MVP: Onboarding data cleared")
    }
    
    override suspend fun markOnboardingCompleted() {
        isCompleted = true
        println("Android MVP: Onboarding marked as completed")
    }
} 