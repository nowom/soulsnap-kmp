package pl.soulsnaps.features.integration

import pl.soulsnaps.features.auth.controller.AppScopeController
import pl.soulsnaps.features.auth.guard.ScopeGuard
import pl.soulsnaps.features.auth.model.*
import pl.soulsnaps.features.memoryanalysis.service.MemoryAnalysisService
import pl.soulsnaps.domain.model.Memory

/**
 * AppScopeIntegration - Integracja systemu user scopes ze wszystkimi modułami SoulSnaps
 * Zapewnia spójne zarządzanie dostępem do funkcji w całej aplikacji
 */
class AppScopeIntegration(
    private val appScopeController: AppScopeController,
    private val scopeGuard: ScopeGuard,
    private val memoryAnalysisService: MemoryAnalysisService
) {
    
    /**
     * Inicjalizacja użytkownika w systemie
     */
    suspend fun initializeUser(userId: String, initialPlan: SubscriptionPlan = SubscriptionPlan.FREE): Boolean {
        return appScopeController.initializeUserScope(userId, initialPlan)
    }
    
    /**
     * Sprawdzenie dostępu do funkcji przed jej użyciem
     */
    suspend fun <T> withFeatureAccess(
        userId: String,
        feature: AppFeature,
        onRestricted: (FeatureRestrictionInfo) -> Unit = {},
        onSuccess: suspend () -> T
    ): T? {
        val canAccess = appScopeController.canAccessAppFeature(userId, feature, onRestricted)
        
        return if (canAccess) {
            onSuccess()
        } else {
            null
        }
    }
    
    /**
     * Sprawdzenie możliwości wykonania akcji przed jej wykonaniem
     */
    suspend fun <T> withActionPermission(
        userId: String,
        action: AppAction,
        onRestricted: (ActionRestrictionInfo) -> Unit = {},
        onSuccess: suspend () -> T
    ): T? {
        val canPerform = appScopeController.canPerformAppAction(userId, action, onRestricted)
        
        return if (canPerform) {
            onSuccess()
        } else {
            null
        }
    }
    
    /**
     * Integracja z Memory Analysis Service
     */
    suspend fun analyzeMemoryWithScope(
        userId: String,
        memory: Memory,
        onRestricted: (String) -> Unit = {}
    ) = withActionPermission(
        userId = userId,
        action = AppAction.AnalyzeMemory(AnalysisType.SINGLE_MEMORY),
        onRestricted = { restrictionInfo ->
            onRestricted("Brak uprawnień do analizy wspomnień")
        }
    ) {
        memoryAnalysisService.analyzeMemory(userId, memory)
    }
    
    suspend fun analyzeMemoriesWithScope(
        userId: String,
        memories: List<Memory>,
        onRestricted: (String) -> Unit = {}
    ) = withFeatureAccess(
        userId = userId,
        feature = AppFeature.PatternDetection,
        onRestricted = { restrictionInfo ->
            onRestricted("Wykrywanie wzorców dostępne od planu Premium")
        }
    ) {
        memoryAnalysisService.analyzeMemories(userId, memories)
    }
    
    suspend fun getAnalysisCapabilitiesWithScope(userId: String) = withFeatureAccess(
        userId = userId,
        feature = AppFeature.MemoryAnalysis,
        onRestricted = { restrictionInfo ->
            // Return default restriction info
        }
    ) {
        memoryAnalysisService.getAnalysisCapabilities(userId)
    }
    
    /**
     * Integracja z Memory Capture
     */
    suspend fun canCaptureMemory(
        userId: String,
        memoryType: MemoryType,
        onRestricted: (ActionRestrictionInfo) -> Unit = {}
    ): Boolean {
        return appScopeController.canPerformAppAction(
            userId = userId,
            appAction = AppAction.CreateMemory(memoryType),
            onRestricted = onRestricted
        )
    }
    
    /**
     * Integracja z Sharing
     */
    suspend fun canShareMemory(
        userId: String,
        shareType: ShareType,
        recipientCount: Int,
        onRestricted: (ActionRestrictionInfo) -> Unit = {}
    ): Boolean {
        return appScopeController.canPerformAppAction(
            userId = userId,
            appAction = AppAction.ShareMemory(shareType, recipientCount),
            onRestricted = onRestricted
        )
    }
    
    /**
     * Integracja z Export
     */
    suspend fun canExportData(
        userId: String,
        format: ExportFormat,
        dataSize: Int,
        onRestricted: (ActionRestrictionInfo) -> Unit = {}
    ): Boolean {
        return appScopeController.canPerformAppAction(
            userId = userId,
            appAction = AppAction.ExportData(format, dataSize),
            onRestricted = onRestricted
        )
    }
    
    /**
     * Integracja z Backup
     */
    suspend fun canBackupData(
        userId: String,
        backupType: BackupType,
        dataSize: Int,
        onRestricted: (ActionRestrictionInfo) -> Unit = {}
    ): Boolean {
        return appScopeController.canPerformAppAction(
            userId = userId,
            appAction = AppAction.BackupData(backupType, dataSize),
            onRestricted = onRestricted
        )
    }
    
    /**
     * Integracja z Customization
     */
    suspend fun canCustomizeApp(
        userId: String,
        customizationType: CustomizationType,
        onRestricted: (ActionRestrictionInfo) -> Unit = {}
    ): Boolean {
        return appScopeController.canPerformAppAction(
            userId = userId,
            appAction = AppAction.CustomizeApp(customizationType),
            onRestricted = onRestricted
        )
    }
    
    /**
     * Pobranie pełnego statusu użytkownika
     */
    suspend fun getUserStatus(userId: String): UserAppStatus {
        return appScopeController.getUserAppStatus(userId)
    }
    
    /**
     * Sprawdzenie limitów użytkownika
     */
    suspend fun checkUserLimits(userId: String): UserLimitsStatus {
        return appScopeController.checkUserLimits(userId)
    }
    
    /**
     * Upgrade planu użytkownika
     */
    suspend fun upgradeUserPlan(
        userId: String,
        newPlan: SubscriptionPlan,
        onSuccess: (UserScope) -> Unit = {},
        onError: (String) -> Unit = {}
    ): Boolean {
        return appScopeController.upgradeUserPlan(userId, newPlan, onSuccess, onError)
    }
    
    /**
     * Pobranie rekomendacji upgrade'u
     */
    suspend fun getUpgradeRecommendations(
        userId: String,
        features: List<FeatureCategory>
    ): List<FeatureUpgradeRecommendation> {
        return appScopeController.getFeatureUpgradeRecommendations(userId, features)
    }
    
    /**
     * Sprawdzenie czy użytkownik potrzebuje upgrade'u dla funkcji
     */
    suspend fun needsUpgradeForFeature(
        userId: String,
        feature: FeatureCategory
    ): Boolean {
        return scopeGuard.checkUpgradeNeeded(userId, feature)
    }
    
    /**
     * Pobranie informacji o planie użytkownika
     */
    suspend fun getCurrentPlanInfo(userId: String): PlanInfo? {
        return scopeGuard.getCurrentPlanInfo(userId)
    }
    
    /**
     * Pobranie podsumowania dostępu do funkcji
     */
    suspend fun getFeatureAccessSummary(userId: String): FeatureAccessSummary {
        return scopeGuard.getFeatureAccessSummary(userId)
    }
    
    /**
     * Sprawdzenie dostępu do wielu funkcji jednocześnie
     */
    suspend fun canAccessMultipleFeatures(
        userId: String,
        features: List<FeatureCategory>
    ): Map<FeatureCategory, Boolean> {
        return scopeGuard.canAccessMultipleFeatures(userId, features)
    }
    
    /**
     * Sprawdzenie czy funkcja jest dostępna z określonym poziomem uprawnień
     */
    suspend fun hasPermissionLevel(
        userId: String,
        feature: FeatureCategory,
        requiredLevel: PermissionLevel
    ): Boolean {
        return scopeGuard.guardFeatureWithLevel(userId, feature, requiredLevel)
    }
    
    /**
     * Pobranie rekomendacji upgrade'u dla funkcji analizy
     */
    suspend fun getAnalysisUpgradeRecommendations(userId: String): List<UpgradeRecommendation> {
        return memoryAnalysisService.getAnalysisUpgradeRecommendations(userId)
    }
    
    /**
     * Sprawdzenie dostępu do funkcji analizy
     */
    suspend fun canAccessAnalysisFeature(
        userId: String,
        feature: FeatureCategory
    ): Boolean {
        return memoryAnalysisService.canAccessAnalysisFeature(userId, feature)
    }
}

/**
 * Extension functions dla łatwiejszego użycia
 */
suspend inline fun <T> AppScopeIntegration.withFeatureAccess(
    userId: String,
    feature: AppFeature,
    noinline onRestricted: (FeatureRestrictionInfo) -> Unit = {},
    noinline onSuccess: suspend () -> T
): T? = withFeatureAccess(userId, feature, onRestricted, onSuccess)

suspend inline fun <T> AppScopeIntegration.withActionPermission(
    userId: String,
    action: AppAction,
    noinline onRestricted: (ActionRestrictionInfo) -> Unit = {},
    noinline onSuccess: suspend () -> T
): T? = withActionPermission(userId, action, onRestricted, onSuccess)
