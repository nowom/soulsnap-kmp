package pl.soulsnaps.features.onboarding

import kotlinx.coroutines.flow.Flow

interface OnboardingDataStore {
    val onboardingCompleted: Flow<Boolean>

    suspend fun clearOnboardingData()
    suspend fun markOnboardingCompleted()
} 