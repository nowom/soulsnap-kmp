package pl.soulsnaps.features.capturemoment

import pl.soulsnaps.domain.model.MoodType
import pl.soulsnaps.access.guard.QuotaInfo
import pl.soulsnaps.utils.getCurrentTimeMillis

data class CaptureMomentState(
    val title: String = "",
    val description: String = "",
    val date: Long = getCurrentTimeMillis(),
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
    val capacityInfo: QuotaInfo? = null,
    val showPaywall: Boolean = false,
    val paywallReason: String? = null,
    val recommendedPlan: String? = null,
    val isCheckingCapacity: Boolean = false,
    
    // New fields for analytics
    val showAnalytics: Boolean = false,
    val analyticsData: pl.soulsnaps.features.analytics.CapacityUsageStats? = null,
    val analyticsAlerts: List<pl.soulsnaps.features.analytics.CapacityAlert> = emptyList(),
    
    // New fields for affirmation
    val affirmationRequested: Boolean = true,
    val generatedAffirmationData: pl.soulsnaps.domain.model.AffirmationData? = null,
    val isGeneratingAffirmation: Boolean = false,
    val affirmationError: String? = null,
    val showAffirmationSnackbar: Boolean = false,
    val showAffirmationDialog: Boolean = false
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
    
    // New intents for affirmation
    data class ToggleAffirmationRequested(val requested: Boolean) : CaptureMomentIntent()
    object GenerateAffirmation : CaptureMomentIntent()
    object ShowAffirmationDialog : CaptureMomentIntent()
    object HideAffirmationDialog : CaptureMomentIntent()
    object DismissAffirmationSnackbar : CaptureMomentIntent()
}