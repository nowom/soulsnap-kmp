package pl.soulsnaps.features.auth.model

/**
 * User Scope System for SoulSnaps
 * Controls access to different features based on user type and subscription
 */

// User Roles
enum class UserRole {
    FREE_USER,           // Darmowy użytkownik - podstawowe funkcje
    PREMIUM_USER,        // Płatny użytkownik - rozszerzone funkcje
    FAMILY_USER,         // Rodzinny plan - funkcje rodzinne
    ENTERPRISE_USER,     // Firmowy plan - zaawansowane funkcje
    ADMIN,               // Administrator - pełny dostęp
    MODERATOR            // Moderator - zarządzanie treściami
}

// Subscription Plans
enum class SubscriptionPlan {
    FREE,                // Darmowy plan
    BASIC,               // Podstawowy plan płatny
    PREMIUM,             // Premium plan
    FAMILY,              // Rodzinny plan
    ENTERPRISE,          // Firmowy plan
    LIFETIME             // Dożywotni dostęp
}

// Feature Categories
enum class FeatureCategory {
    MEMORY_CAPTURE,      // Podstawowe przechwytywanie wspomnień
    MEMORY_ANALYSIS,     // Analiza wspomnień i AI
    PATTERN_DETECTION,   // Wykrywanie wzorców
    INSIGHTS,            // Szczegółowe insights
    SHARING,             // Udostępnianie z innymi
    COLLABORATION,       // Współpraca rodzinna
    EXPORT,              // Eksport danych
    BACKUP,              // Backup i synchronizacja
    CUSTOMIZATION,       // Personalizacja
    ADVANCED_AI,         // Zaawansowane funkcje AI
    API_ACCESS,          // Dostęp do API
    SUPPORT             // Wsparcie techniczne
}

// Permission Levels
enum class PermissionLevel {
    NONE,               // Brak dostępu
    READ_ONLY,          // Tylko odczyt
    BASIC,              // Podstawowe funkcje
    STANDARD,           // Standardowe funkcje
    ADVANCED,           // Zaawansowane funkcje
    FULL                // Pełny dostęp
}

// User Scope Configuration
data class UserScope(
    val userId: String,
    val role: UserRole,
    val subscriptionPlan: SubscriptionPlan,
    val permissions: Map<FeatureCategory, PermissionLevel>,
    val limits: UserLimits,
    val restrictions: List<FeatureRestriction>,
    val validUntil: Long?, // Timestamp when scope expires
    val isActive: Boolean = true
)

// User Limits
data class UserLimits(
    val maxMemories: Int,           // Maksymalna liczba wspomnień
    val maxStorageGB: Int,          // Maksymalny rozmiar przechowywania
    val maxAnalysisPerDay: Int,     // Maksymalna liczba analiz dziennie
    val maxCollaborators: Int,      // Maksymalna liczba współpracowników
    val maxExportsPerMonth: Int,    // Maksymalna liczba eksportów miesięcznie
    val maxBackups: Int,            // Maksymalna liczba backupów
    val retentionDays: Int          // Dni przechowywania danych
)

// Feature Restrictions
data class FeatureRestriction(
    val feature: FeatureCategory,
    val restrictionType: RestrictionType,
    val value: Any?, // Limit value or restriction details
    val message: String // User-friendly message about restriction
)

enum class RestrictionType {
    QUOTA_EXCEEDED,     // Przekroczono limit
    FEATURE_DISABLED,   // Funkcja wyłączona
    UPGRADE_REQUIRED,   // Wymagane uaktualnienie
    PAYMENT_REQUIRED,   // Wymagana płatność
    VERIFICATION_NEEDED, // Wymagana weryfikacja
    TIME_LIMITED        // Ograniczenie czasowe
}

// Feature Access Control
object FeatureAccessControl {
    
    /**
     * Check if user has access to specific feature
     */
    fun hasAccess(userScope: UserScope, feature: FeatureCategory): Boolean {
        val permission = userScope.permissions[feature] ?: PermissionLevel.NONE
        return permission != PermissionLevel.NONE
    }
    
    /**
     * Check if user has specific permission level
     */
    fun hasPermissionLevel(userScope: UserScope, feature: FeatureCategory, requiredLevel: PermissionLevel): Boolean {
        val permission = userScope.permissions[feature] ?: PermissionLevel.NONE
        return permission.ordinal >= requiredLevel.ordinal
    }
    
    /**
     * Check if user can perform action within limits
     */
    fun canPerformAction(userScope: UserScope, action: UserAction): Boolean {
        return when (action) {
            is UserAction.CreateMemory -> {
                val currentCount = getCurrentMemoryCount(userScope.userId)
                currentCount < userScope.limits.maxMemories
            }
            is UserAction.AnalyzeMemory -> {
                val dailyCount = getDailyAnalysisCount(userScope.userId)
                dailyCount < userScope.limits.maxAnalysisPerDay
            }
            is UserAction.ExportData -> {
                val monthlyCount = getMonthlyExportCount(userScope.userId)
                monthlyCount < userScope.limits.maxExportsPerMonth
            }
            is UserAction.ShareMemory -> {
                hasAccess(userScope, FeatureCategory.SHARING)
            }
            is UserAction.Collaborate -> {
                val currentCollaborators = getCurrentCollaboratorsCount(userScope.userId)
                currentCollaborators < userScope.limits.maxCollaborators
            }
        }
    }
    
    /**
     * Get user-friendly restriction message
     */
    fun getRestrictionMessage(userScope: UserScope, feature: FeatureCategory): String? {
        val restriction = userScope.restrictions.find { it.feature == feature }
        return restriction?.message
    }
    
    /**
     * Check if user needs to upgrade for feature
     */
    fun needsUpgrade(userScope: UserScope, feature: FeatureCategory): Boolean {
        val restriction = userScope.restrictions.find { it.feature == feature }
        return restriction?.restrictionType == RestrictionType.UPGRADE_REQUIRED
    }
    
    // Helper functions (would be implemented with actual data)
    private fun getCurrentMemoryCount(userId: String): Int = 0 // TODO: Implement
    private fun getDailyAnalysisCount(userId: String): Int = 0 // TODO: Implement
    private fun getMonthlyExportCount(userId: String): Int = 0 // TODO: Implement
    private fun getCurrentCollaboratorsCount(userId: String): Int = 0 // TODO: Implement
}

// User Actions
sealed class UserAction {
    data class CreateMemory(val memoryType: String) : UserAction()
    data class AnalyzeMemory(val analysisType: String) : UserAction()
    data class ExportData(val format: String) : UserAction()
    data class ShareMemory(val shareType: String) : UserAction()
    data class Collaborate(val collaborationType: String) : UserAction()
}

// Default Scope Configurations
object DefaultScopes {
    
    fun getFreeUserScope(userId: String): UserScope {
        return UserScope(
            userId = userId,
            role = UserRole.FREE_USER,
            subscriptionPlan = SubscriptionPlan.FREE,
            permissions = mapOf(
                FeatureCategory.MEMORY_CAPTURE to PermissionLevel.BASIC,
                FeatureCategory.MEMORY_ANALYSIS to PermissionLevel.READ_ONLY,
                FeatureCategory.PATTERN_DETECTION to PermissionLevel.NONE,
                FeatureCategory.INSIGHTS to PermissionLevel.READ_ONLY,
                FeatureCategory.SHARING to PermissionLevel.NONE,
                FeatureCategory.COLLABORATION to PermissionLevel.NONE,
                FeatureCategory.EXPORT to PermissionLevel.NONE,
                FeatureCategory.BACKUP to PermissionLevel.NONE,
                FeatureCategory.CUSTOMIZATION to PermissionLevel.BASIC,
                FeatureCategory.ADVANCED_AI to PermissionLevel.NONE,
                FeatureCategory.API_ACCESS to PermissionLevel.NONE,
                FeatureCategory.SUPPORT to PermissionLevel.BASIC
            ),
            limits = UserLimits(
                maxMemories = 50,
                maxStorageGB = 1,
                maxAnalysisPerDay = 5,
                maxCollaborators = 0,
                maxExportsPerMonth = 0,
                maxBackups = 0,
                retentionDays = 30
            ),
            restrictions = listOf(
                FeatureRestriction(
                    feature = FeatureCategory.MEMORY_ANALYSIS,
                    restrictionType = RestrictionType.QUOTA_EXCEEDED,
                    value = 5,
                    message = "Dzienny limit analiz (5) został przekroczony. Uaktualnij plan, aby uzyskać więcej."
                ),
                FeatureRestriction(
                    feature = FeatureCategory.PATTERN_DETECTION,
                    restrictionType = RestrictionType.UPGRADE_REQUIRED,
                    value = null,
                    message = "Wykrywanie wzorców dostępne tylko w planach Premium. Uaktualnij swój plan!"
                )
            ),
            validUntil = null,
            isActive = true
        )
    }
    
    fun getPremiumUserScope(userId: String): UserScope {
        return UserScope(
            userId = userId,
            role = UserRole.PREMIUM_USER,
            subscriptionPlan = SubscriptionPlan.PREMIUM,
            permissions = mapOf(
                FeatureCategory.MEMORY_CAPTURE to PermissionLevel.FULL,
                FeatureCategory.MEMORY_ANALYSIS to PermissionLevel.ADVANCED,
                FeatureCategory.PATTERN_DETECTION to PermissionLevel.STANDARD,
                FeatureCategory.INSIGHTS to PermissionLevel.ADVANCED,
                FeatureCategory.SHARING to PermissionLevel.STANDARD,
                FeatureCategory.COLLABORATION to PermissionLevel.BASIC,
                FeatureCategory.EXPORT to PermissionLevel.STANDARD,
                FeatureCategory.BACKUP to PermissionLevel.STANDARD,
                FeatureCategory.CUSTOMIZATION to PermissionLevel.ADVANCED,
                FeatureCategory.ADVANCED_AI to PermissionLevel.BASIC,
                FeatureCategory.API_ACCESS to PermissionLevel.NONE,
                FeatureCategory.SUPPORT to PermissionLevel.STANDARD
            ),
            limits = UserLimits(
                maxMemories = 1000,
                maxStorageGB = 10,
                maxAnalysisPerDay = 100,
                maxCollaborators = 5,
                maxExportsPerMonth = 10,
                maxBackups = 5,
                retentionDays = 365
            ),
            restrictions = listOf(
                FeatureRestriction(
                    feature = FeatureCategory.ADVANCED_AI,
                    restrictionType = RestrictionType.UPGRADE_REQUIRED,
                    value = null,
                    message = "Zaawansowane funkcje AI dostępne w planie Enterprise. Uaktualnij plan!"
                )
            ),
            validUntil = null,
            isActive = true
        )
    }
    
    fun getFamilyUserScope(userId: String): UserScope {
        return UserScope(
            userId = userId,
            role = UserRole.FAMILY_USER,
            subscriptionPlan = SubscriptionPlan.FAMILY,
            permissions = mapOf(
                FeatureCategory.MEMORY_CAPTURE to PermissionLevel.FULL,
                FeatureCategory.MEMORY_ANALYSIS to PermissionLevel.STANDARD,
                FeatureCategory.PATTERN_DETECTION to PermissionLevel.STANDARD,
                FeatureCategory.INSIGHTS to PermissionLevel.STANDARD,
                FeatureCategory.SHARING to PermissionLevel.ADVANCED,
                FeatureCategory.COLLABORATION to PermissionLevel.ADVANCED,
                FeatureCategory.EXPORT to PermissionLevel.STANDARD,
                FeatureCategory.BACKUP to PermissionLevel.STANDARD,
                FeatureCategory.CUSTOMIZATION to PermissionLevel.STANDARD,
                FeatureCategory.ADVANCED_AI to PermissionLevel.BASIC,
                FeatureCategory.API_ACCESS to PermissionLevel.NONE,
                FeatureCategory.SUPPORT to PermissionLevel.STANDARD
            ),
            limits = UserLimits(
                maxMemories = 5000,
                maxStorageGB = 25,
                maxAnalysisPerDay = 200,
                maxCollaborators = 10,
                maxExportsPerMonth = 25,
                maxBackups = 10,
                retentionDays = 730
            ),
            restrictions = emptyList(),
            validUntil = null,
            isActive = true
        )
    }
}
