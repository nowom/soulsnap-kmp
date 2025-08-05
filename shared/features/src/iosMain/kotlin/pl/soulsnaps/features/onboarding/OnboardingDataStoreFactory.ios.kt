package pl.soulsnaps.features.onboarding

actual fun createOnboardingDataStore(): OnboardingDataStore {
    return IOSOnboardingDataStore()
} 