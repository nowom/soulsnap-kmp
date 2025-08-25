package pl.soulsnaps.features.auth.mvp.guard.model

import kotlinx.serialization.Serializable

/**
 * UserAction represents high-level user actions that can be restricted
 * These are mapped to AppAction for detailed permission checking
 */
@Serializable
sealed class UserAction {
    
    // Memory Actions
    @Serializable
    data class CreateMemory(val memoryType: String) : UserAction()
    
    @Serializable
    data class AnalyzeMemory(val analysisType: String) : UserAction()
    
    @Serializable
    data class ExportData(val format: String) : UserAction()
    
    @Serializable
    data class ShareMemory(val shareType: String) : UserAction()
    
    @Serializable
    data class Collaborate(val collaborationType: String) : UserAction()
    
    // AI Actions
    @Serializable
    data class UseAI(val aiType: String) : UserAction()
    
    // Premium Feature Actions
    @Serializable
    data class ExportVideo(val quality: String = "hd") : UserAction()
    
    @Serializable
    data class BackupToCloud(val cloudProvider: String = "default") : UserAction()
    
    @Serializable
    data class AdvancedFilters(val filterType: String) : UserAction()
    
    @Serializable
    data class AudioAttach(val audioType: String = "voice") : UserAction()
    
    // Enterprise Actions
    @Serializable
    data class TeamCollaboration(val teamSize: Int) : UserAction()
    
    @Serializable
    data class AnalyticsAccess(val dashboardType: String) : UserAction()
    
    @Serializable
    data class APIAccess(val endpoint: String) : UserAction()
}

