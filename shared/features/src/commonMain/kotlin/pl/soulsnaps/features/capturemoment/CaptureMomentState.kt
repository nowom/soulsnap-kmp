package pl.soulsnaps.features.capturemoment

import pl.soulsnaps.domain.model.MoodType
import pl.soulsnaps.features.auth.mvp.guard.CapacityInfo
import kotlinx.datetime.Clock

data class CaptureMomentState(
    val title: String = "",
    val description: String = "",
    val date: Long = Clock.System.now().toEpochMilliseconds(),
    val selectedMood: MoodType? = null,
    val photoUri: String? = null,
    val audioUri: String? = null,
    val location: String? = null,
    val generatedAffirmation: String? = null,
    val isTitleValid: Boolean? = null,
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val savedMemoryId: Int? = null,
    
    // New fields for capacity management
    val capacityInfo: CapacityInfo? = null,
    val showPaywall: Boolean = false,
    val paywallReason: String? = null,
    val recommendedPlan: String? = null,
    val isCheckingCapacity: Boolean = false,
    
    // New fields for analytics
    val showAnalytics: Boolean = false,
    val analyticsData: pl.soulsnaps.features.analytics.CapacityUsageStats? = null,
    val analyticsAlerts: List<pl.soulsnaps.features.analytics.CapacityAlert> = emptyList()
)

sealed class CaptureMomentIntent {
    data class ChangeTitle(val title: String) : CaptureMomentIntent()
    data class ChangeDescription(val description: String) : CaptureMomentIntent()
    data class ChangeDate(val date: Long) : CaptureMomentIntent()
    data class ChangeMood(val mood: MoodType) : CaptureMomentIntent()
    data class ChangePhoto(val photoUri: String?) : CaptureMomentIntent()
    data class ChangeLocation(val location: String) : CaptureMomentIntent()
    data class ChangeAudio(val audioUri: String) : CaptureMomentIntent()
    object SaveMemory : CaptureMomentIntent()
    object ClearMessages : CaptureMomentIntent()
    
    // New intents for capacity management
    object CheckCapacity : CaptureMomentIntent()
    object ShowPaywall : CaptureMomentIntent()
    data class NavigateToPaywall(val reason: String, val recommendedPlan: String?) : CaptureMomentIntent()
    
    // New intents for analytics
    object ShowAnalytics : CaptureMomentIntent()
    object UpdateAnalytics : CaptureMomentIntent()
}