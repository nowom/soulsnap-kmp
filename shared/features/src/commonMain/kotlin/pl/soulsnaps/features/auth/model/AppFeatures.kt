package pl.soulsnaps.features.auth.model

import pl.soulsnaps.utils.getCurrentTimeMillis

/**
 * App Features - Funkcje dostępne w aplikacji SoulSnaps
 * Każda funkcja ma przypisaną kategorię i może być chroniona przez scope
 */

sealed class AppFeature(val featureCategory: FeatureCategory) {
    
    // Memory Management
    object MemoryCapture : AppFeature(FeatureCategory.MEMORY_CAPTURE)
    object MemoryAnalysis : AppFeature(FeatureCategory.MEMORY_ANALYSIS)
    object PatternDetection : AppFeature(FeatureCategory.PATTERN_DETECTION)
    object Insights : AppFeature(FeatureCategory.INSIGHTS)
    
    // Social Features
    object Sharing : AppFeature(FeatureCategory.SHARING)
    object Collaboration : AppFeature(FeatureCategory.COLLABORATION)
    
    // Data Management
    object Export : AppFeature(FeatureCategory.EXPORT)
    object Backup : AppFeature(FeatureCategory.BACKUP)
    
    // Customization
    object Customization : AppFeature(FeatureCategory.CUSTOMIZATION)
    
    // Advanced Features
    object AdvancedAI : AppFeature(FeatureCategory.ADVANCED_AI)
    object APIAccess : AppFeature(FeatureCategory.API_ACCESS)
    
    // Support
    object Support : AppFeature(FeatureCategory.SUPPORT)
}

/**
 * App Actions - Akcje wykonywane przez użytkowników w aplikacji
 * Każda akcja może być chroniona przez scope i limity
 */

sealed class AppAction {
    
    // Memory Actions
    data class CreateMemory(
        val memoryType: MemoryType,
        val hasImage: Boolean = false,
        val hasAudio: Boolean = false,
        val hasLocation: Boolean = false
    ) : AppAction()
    
    data class AnalyzeMemory(
        val analysisType: AnalysisType,
        val memoryCount: Int = 1
    ) : AppAction()
    
    data class ShareMemory(
        val shareType: ShareType,
        val recipientCount: Int = 1
    ) : AppAction()
    
    // Data Actions
    data class ExportData(
        val format: ExportFormat,
        val dataSize: Int = 1
    ) : AppAction()
    
    data class BackupData(
        val backupType: BackupType,
        val dataSize: Int = 1
    ) : AppAction()
    
    // Customization Actions
    data class CustomizeApp(
        val customizationType: CustomizationType
    ) : AppAction()
    
    // Helper function to convert to UserAction
    fun toUserAction(): UserAction {
        return when (this) {
            is CreateMemory -> UserAction.CreateMemory(memoryType.name)
            is AnalyzeMemory -> UserAction.AnalyzeMemory(analysisType.name)
            is ShareMemory -> UserAction.ShareMemory(shareType.name)
            is ExportData -> UserAction.ExportData(format.name)
            is BackupData -> UserAction.Collaborate(backupType.name)
            is CustomizeApp -> UserAction.Collaborate(customizationType.name)
        }
    }
}

// Enums for different types
enum class MemoryType {
    PHOTO, VIDEO, AUDIO, TEXT, LOCATION, MIXED
}

enum class AnalysisType {
    SINGLE_MEMORY, BATCH_ANALYSIS, PATTERN_DETECTION, MOOD_ANALYSIS, COLOR_ANALYSIS, FACE_DETECTION
}

enum class ShareType {
    PRIVATE, FAMILY, FRIENDS, PUBLIC, COLLABORATIVE
}

enum class ExportFormat {
    JSON, CSV, PDF, IMAGE, VIDEO, AUDIO, ARCHIVE
}

enum class BackupType {
    LOCAL, CLOUD, EXTERNAL, AUTOMATIC
}

enum class CustomizationType {
    THEME, LAYOUT, NOTIFICATIONS, SHORTCUTS, WIDGETS
}

/**
 * Feature Restriction Info - Informacje o ograniczeniach funkcji
 */
data class FeatureRestrictionInfo(
    val feature: FeatureCategory,
    val restrictionType: RestrictionType,
    val message: String,
    val requiredAction: RequiredAction,
    val currentPlan: SubscriptionPlan? = null,
    val recommendedPlan: SubscriptionPlan? = null
)

/**
 * Action Restriction Info - Informacje o ograniczeniach akcji
 */
data class ActionRestrictionInfo(
    val action: AppAction,
    val message: String,
    val requiredAction: RequiredAction,
    val currentLimit: Int? = null,
    val limitType: String? = null
)

/**
 * Required Action - Akcja wymagana od użytkownika
 */
enum class RequiredAction {
    UPGRADE_PLAN,           // Uaktualnij plan
    RENEW_SUBSCRIPTION,     // Odnów subskrypcję
    VERIFY_ACCOUNT,         // Zweryfikuj konto
    WAIT_FOR_RESET,        // Poczekaj na reset limitu
    CONTACT_SUPPORT,        // Skontaktuj się z supportem
    PAYMENT_REQUIRED        // Wymagana płatność
}

/**
 * User App Status - Pełny status użytkownika w aplikacji
 */
sealed class UserAppStatus {
    object NotFound : UserAppStatus()
    object Inactive : UserAppStatus()
    
    data class Active(
        val userScope: UserScope,
        val featureSummary: FeatureAccessSummary,
        val upgradeRecommendations: List<UpgradeRecommendation>,
        val usageStats: UserUsageStats,
        val nextBillingDate: Long?
    ) : UserAppStatus()
}

/**
 * User Usage Stats - Statystyki użycia przez użytkownika
 */
data class UserUsageStats(
    val memoriesCount: Int,
    val storageUsedGB: Float,
    val dailyAnalysisCount: Int,
    val monthlyExportsCount: Int,
    val lastActivity: Long = getCurrentTimeMillis()
)

/**
 * User Limits Status - Status limitów użytkownika
 */
sealed class UserLimitsStatus {
    object NotFound : UserLimitsStatus()
    
    data class Active(
        val memories: LimitStatus,
        val storage: LimitStatus,
        val dailyAnalysis: LimitStatus,
        val monthlyExports: LimitStatus
    ) : UserLimitsStatus()
}

/**
 * Limit Status - Status konkretnego limitu
 */
data class LimitStatus(
    val current: Int,
    val limit: Int,
    val percentage: Float,
    val isExceeded: Boolean = percentage > 100,
    val remaining: Int = maxOf(0, limit - current)
)

/**
 * Feature Upgrade Recommendation - Rekomendacja upgrade'u dla funkcji
 */
data class FeatureUpgradeRecommendation(
    val feature: FeatureCategory,
    val currentPlan: SubscriptionPlan,
    val recommendedPlan: SubscriptionPlan,
    val reason: String,
    val benefits: List<String>,
    val estimatedCost: String,
    val priority: UpgradePriority = UpgradePriority.MEDIUM
)

/**
 * Upgrade Priority - Priorytet upgrade'u
 */
enum class UpgradePriority {
    LOW,        // Opcjonalny upgrade
    MEDIUM,     // Zalecany upgrade
    HIGH,       // Ważny upgrade
    CRITICAL    // Krytyczny upgrade
}

/**
 * Feature Access Summary - Podsumowanie dostępu do funkcji
 */
data class FeatureAccessSummary(
    val accessibleFeatures: List<FeatureCategory>,
    val restrictedFeatures: List<FeatureCategory>,
    val upgradeRecommendations: List<UpgradeRecommendation>
)

/**
 * Upgrade Recommendation - Rekomendacja upgrade'u dla funkcji
 */
data class UpgradeRecommendation(
    val feature: FeatureCategory,
    val currentPlan: SubscriptionPlan,
    val recommendedPlan: SubscriptionPlan,
    val reason: String,
    val benefits: List<String>,
    val estimatedCost: String,
    val priority: UpgradePriority = UpgradePriority.MEDIUM
)
