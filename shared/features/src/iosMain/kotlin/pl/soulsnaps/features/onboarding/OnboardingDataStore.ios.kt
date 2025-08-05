package pl.soulsnaps.features.onboarding

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

// For iOS MVP, we'll use in-memory storage
// In production, this would use UserDefaults or Core Data
class IOSOnboardingDataStore : OnboardingDataStore {
    
    private val _onboardingCompleted = MutableStateFlow(false)
    private val _onboardingData = MutableStateFlow<OnboardingData?>(null)
    
    override val onboardingCompleted: Flow<Boolean> = _onboardingCompleted
    
    override suspend fun clearOnboardingData() {
        _onboardingCompleted.value = false
        _onboardingData.value = null
        println("iOS: Onboarding data cleared")
    }
    
    override suspend fun markOnboardingCompleted() {
        _onboardingCompleted.value = true
        println("iOS: Onboarding marked as completed")
    }
} 