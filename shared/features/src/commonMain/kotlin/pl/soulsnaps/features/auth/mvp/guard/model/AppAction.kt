package pl.soulsnaps.features.auth.mvp.guard.model

import kotlinx.serialization.Serializable

/**
 * AppAction represents different actions users can perform in the app
 * Each action can be restricted based on user's plan and limits
 */
@Serializable
sealed class AppAction {
    
    // Memory Actions
    @Serializable
    data class CreateMemory(
        val memoryType: String,
        val hasImage: Boolean = false,
        val hasAudio: Boolean = false,
        val hasLocation: Boolean = false
    ) : AppAction()
    
    @Serializable
    data class AnalyzeMemory(
        val analysisType: String,
        val memoryCount: Int = 1
    ) : AppAction()
    
    @Serializable
    data class ShareMemory(
        val shareType: String,
        val recipientCount: Int = 1
    ) : AppAction()
    
    // Data Actions
    @Serializable
    data class ExportData(
        val format: String,
        val dataSize: Int = 1
    ) : AppAction()
    
    @Serializable
    data class BackupData(
        val backupType: String,
        val dataSize: Int = 1
    ) : AppAction()
    
    // Customization Actions
    @Serializable
    data class CustomizeApp(
        val customizationType: String
    ) : AppAction()
    
    // AI Actions
    @Serializable
    data class UseAI(
        val aiType: String,
        val complexity: String = "basic"
    ) : AppAction()
    
    // Feature Actions
    @Serializable
    data class ExportVideo(
        val quality: String = "hd"
    ) : AppAction()
    
    @Serializable
    data class BackupToCloud(
        val cloudProvider: String = "default"
    ) : AppAction()
    
    @Serializable
    data class AdvancedFilters(
        val filterType: String
    ) : AppAction()
    
    @Serializable
    data class AudioAttach(
        val audioType: String = "voice"
    ) : AppAction()
}
