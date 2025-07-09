package pl.soulsnaps.features.capturemoment

import pl.soulsnaps.domain.model.MoodType

data class CaptureMomentState(
    val title: String = "",
    val description: String = "",
    val date: Long = 0,
    val selectedMood: MoodType? = null,
    val photoUri: String? = null,
    val audioUri: String? = null,
    val location: String? = null,
    val generatedAffirmation: String? = null,
    val isTitleValid: Boolean? = null,
    val isSaving: Boolean = false,
    val errorMessage: String? = null
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
}