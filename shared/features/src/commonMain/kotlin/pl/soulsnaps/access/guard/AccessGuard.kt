package pl.soulsnaps.access.guard

import pl.soulsnaps.access.guard.ScopePolicy
import pl.soulsnaps.access.guard.QuotaPolicy
import pl.soulsnaps.access.guard.FeatureToggle
import pl.soulsnaps.access.manager.UserPlanManager
import pl.soulsnaps.access.manager.DefaultPlans
import pl.soulsnaps.access.manager.PlanRegistryReaderImpl
import pl.soulsnaps.access.guard.QuotaInfo
import pl.soulsnaps.access.guard.FeatureInfo
import pl.soulsnaps.access.manager.PlanRegistryReader

/**
 * Interface for UserPlanManager to enable testing
 */
interface UserPlanManagerInterface {
    fun setUserPlan(planName: String)
    fun getUserPlan(): String?
    fun isOnboardingCompleted(): Boolean
    fun getPlanOrDefault(): String
    fun hasPlanSet(): Boolean
    fun getCurrentPlan(): String?
}

/**
 * Zasada Dependency Inversion
 * Guard zależy od abstrakcji, nie od konkretnych implementacji
 */

/**
 * Main Guard - Zasada Dependency Inversion
 * Zależy od interfejsów, nie od konkretnych klas
 */
open class AccessGuard(
    protected val scopePolicy: ScopePolicy,
    protected val quotaPolicy: QuotaPolicy,
    protected val featureToggle: FeatureToggle,
    protected val userPlanManager: UserPlanManagerInterface
) {
    
    /**
     * Sprawdź czy użytkownik może wykonać akcję
     */
    suspend fun allowAction(
        userId: String, 
        action: String, 
        quotaKey: String? = null, 
        flagKey: String? = null
    ): AccessResult {
        
        // 1. Sprawdź czy feature jest globalnie włączony
        if (flagKey != null) {
            val isEmergencyFlag = flagKey.startsWith("emergency.")
            val featureEnabled = if (isEmergencyFlag) {
                !featureToggle.isOn(flagKey) // Emergency flags: true = wyłącz funkcję
            } else {
                featureToggle.isOn(flagKey) // Normal flags: true = włącz funkcję
            }
            
            if (!featureEnabled) {
                return AccessResult(
                    allowed = false, 
                    reason = DenyReason.FEATURE_OFF, 
                    message = "Funkcja chwilowo niedostępna",
                    featureInfo = featureToggle.getFeatureInfo(flagKey)
                )
            }
        }
        
        // 2. Sprawdź czy użytkownik ma scope dla akcji
        if (!scopePolicy.hasScope(userId, action)) {
            val recommendedPlan = scopePolicy.getRequiredPlanForAction(action)
            val message = if (recommendedPlan != null) {
                "Funkcja dostępna w planie $recommendedPlan"
            } else {
                "Brak uprawnień do tej akcji"
            }
            
            return AccessResult(
                allowed = false, 
                reason = DenyReason.MISSING_SCOPE, 
                message = message,
                recommendedPlan = recommendedPlan
            )
        }
        
        // 3. Sprawdź i skonsumuj quota jeśli określony
        if (quotaKey != null) {
            if (!quotaPolicy.checkAndConsume(userId, quotaKey)) {
                val quotaInfo = quotaPolicy.getQuotaInfo(userId, quotaKey)
                val message = getQuotaExceededMessage(quotaKey, quotaInfo)
                
                return AccessResult(
                    allowed = false, 
                    reason = DenyReason.QUOTA_EXCEEDED, 
                    message = message,
                    quotaInfo = quotaInfo
                )
            }
        }
        
        return AccessResult(allowed = true)
    }
    
    /**
     * Pobierz aktualny plan użytkownika
     */
    fun getCurrentUserPlan(): String {
        return userPlanManager.getPlanOrDefault()
    }
    
    /**
     * Sprawdź czy użytkownik ukończył onboarding
     */
    fun hasCompletedOnboarding(): Boolean {
        return userPlanManager.isOnboardingCompleted()
    }
    
    /**
     * Ustaw plan użytkownika
     */
    fun setUserPlan(planName: String) {
        userPlanManager.setUserPlan(planName)
    }
    
    /**
     * Sprawdź czy użytkownik może wykonać akcję bez konsumpcji quota
     */
    suspend fun canPerformAction(
        userId: String, 
        action: String, 
        quotaKey: String? = null, 
        flagKey: String? = null
    ): AccessResult {
        
        // 1. Sprawdź czy feature jest globalnie włączony
        if (flagKey != null) {
            val isEmergencyFlag = flagKey.startsWith("emergency.")
            val featureEnabled = if (isEmergencyFlag) {
                !featureToggle.isOn(flagKey) // Emergency flags: true = wyłącz funkcję
            } else {
                featureToggle.isOn(flagKey) // Normal flags: true = włącz funkcję
            }
            
            if (!featureEnabled) {
                return AccessResult(
                    allowed = false, 
                    reason = DenyReason.FEATURE_OFF, 
                    message = "Funkcja chwilowo niedostępna",
                    featureInfo = featureToggle.getFeatureInfo(flagKey)
                )
            }
        }
        
        // 2. Sprawdź czy użytkownik ma scope dla akcji
        if (!scopePolicy.hasScope(userId, action)) {
            val recommendedPlan = scopePolicy.getRequiredPlanForAction(action)
            val message = if (recommendedPlan != null) {
                "Funkcja dostępna w planie $recommendedPlan"
            } else {
                "Brak uprawnień do tej akcji"
            }
            
            return AccessResult(
                allowed = false, 
                reason = DenyReason.MISSING_SCOPE, 
                message = message,
                recommendedPlan = recommendedPlan
            )
        }
        
        // 3. Sprawdź quota bez konsumpcji
        if (quotaKey != null) {
            val remaining = quotaPolicy.getRemaining(userId, quotaKey)
            if (remaining <= 0) {
                val quotaInfo = quotaPolicy.getQuotaInfo(userId, quotaKey)
                val message = getQuotaExceededMessage(quotaKey, quotaInfo)
                
                return AccessResult(
                    allowed = false, 
                    reason = DenyReason.QUOTA_EXCEEDED, 
                    message = message,
                    quotaInfo = quotaInfo
                )
            }
        }
        
        return AccessResult(allowed = true)
    }
    
    /**
     * Pobierz rekomendację upgrade'u dla akcji
     */
    fun getUpgradeRecommendation(action: String): String? {
        return scopePolicy.getRequiredPlanForAction(action)
    }
    
    /**
     * Pobierz status quota użytkownika
     */
    suspend fun getQuotaStatus(userId: String, quotaKey: String): Int {
        return quotaPolicy.getRemaining(userId, quotaKey)
    }
    
    /**
     * Pobierz informacje o quota
     */
    suspend fun getQuotaInfo(userId: String, quotaKey: String): QuotaInfo? {
        return quotaPolicy.getQuotaInfo(userId, quotaKey)
    }
    
    /**
     * Pobierz wszystkie scopes użytkownika
     */
    suspend fun getUserScopes(userId: String): List<String> {
        return scopePolicy.getUserScopes(userId)
    }
    
    /**
     * Pobierz informacje o feature
     */
    fun getFeatureInfo(key: String): FeatureInfo? {
        return featureToggle.getFeatureInfo(key)
    }
    
    /**
     * Pobierz wszystkie features
     */
    fun getAllFeatures(): Map<String, Boolean> {
        return featureToggle.getAllFeatures()
    }
    
    /**
     * Sprawdź czy feature jest włączony
     */
    fun isFeatureEnabled(key: String): Boolean {
        return featureToggle.isOn(key)
    }
    
    // Helper methods
    private fun getQuotaExceededMessage(quotaKey: String, quotaInfo: QuotaInfo?): String {
        return when {
            quotaKey.endsWith(".day") -> "Limit dzienny wyczerpany. Reset o 00:00."
            quotaKey.endsWith(".month") -> "Limit miesięczny wyczerpany. Reset o początku miesiąca."
            quotaKey.endsWith(".year") -> "Limit roczny wyczerpany. Reset o początku roku."
            else -> "Limit wyczerpany"
        }
    }
}

/**
 * Enhanced Access Result - SOLID: Open/Closed
 * Łatwo rozszerzyć o nowe pola bez modyfikacji istniejącego kodu
 */
data class AccessResult(
    val allowed: Boolean, 
    val reason: DenyReason? = null, 
    val message: String? = null,
    val quotaRemaining: Int? = null,
    val quotaInfo: QuotaInfo? = null,
    val featureInfo: FeatureInfo? = null,
    val recommendedPlan: String? = null,
    val userContext: UserContext? = null
)

/**
 * Deny Reason - Open/Closed Principle
 * Łatwo dodać nowe powody odmowy
 */
enum class DenyReason { 
    MISSING_SCOPE,      // Użytkownik nie ma uprawnień
    QUOTA_EXCEEDED,     // Użytkownik przekroczył limit
    FEATURE_OFF,        // Funkcja jest wyłączona globalnie
    USER_SUSPENDED,     // Użytkownik zawieszony
    MAINTENANCE_MODE,   // Tryb konserwacji
    RATE_LIMITED        // Ograniczenie szybkości
}

/**
 * Guard Factory - Factory Pattern
 * Łatwo tworzyć różne konfiguracje guard'a
 */
object GuardFactory {
    
    /**
     * Twórz domyślny guard z in-memory implementacjami
     */
    fun createDefaultGuard(userPlanManager: UserPlanManagerInterface): AccessGuard {
        val planRegistry = PlanRegistryReaderImpl()
        val scopePolicy = InMemoryScopePolicy(planRegistry)
        val quotaPolicy = InMemoryQuotaPolicy(planRegistry, scopePolicy)
        val featureToggle = InMemoryFeatureToggle()
        
        return AccessGuard(scopePolicy, quotaPolicy, featureToggle, userPlanManager)
    }
    
    /**
     * Twórz CapacityGuard z domyślnym AccessGuard
     */
    fun createCapacityGuard(userPlanManager: UserPlanManagerInterface): CapacityGuard {
        val accessGuard = createDefaultGuard(userPlanManager)
        return CapacityGuard(accessGuard, PlanRegistryReaderImpl())
    }
    
    /**
     * Twórz guard z custom implementacjami
     */
    fun createCustomGuard(
        scopePolicy: ScopePolicy,
        quotaPolicy: QuotaPolicy,
        featureToggle: FeatureToggle,
        userPlanManager: UserPlanManagerInterface
    ): AccessGuard {
        return AccessGuard(scopePolicy, quotaPolicy, featureToggle, userPlanManager)
    }
    
    /**
     * Twórz guard dla testów
     */
    fun createTestGuard(userPlanManager: UserPlanManagerInterface): AccessGuard {
        // Można tu dodać mock implementacje dla testów
        return createDefaultGuard(userPlanManager)
    }
}

/**
 * Guard Decorator - Decorator Pattern
 * Łatwo dodać nowe funkcjonalności bez modyfikacji istniejącego kodu
 */
abstract class GuardDecorator(
    protected val guard: AccessGuard
) {
    
    // Dodatkowe funkcjonalności
    abstract fun getExtendedInfo(userId: String): Map<String, Any>
}

/**
 * Logging Guard Decorator - Decorator Pattern
 * Dodaje logowanie do istniejącego guard'a
 */
class LoggingGuardDecorator(
    guard: AccessGuard,
    private val logger: (String) -> Unit
) : GuardDecorator(guard) {
    
    override fun getExtendedInfo(userId: String): Map<String, Any> {
        logger("Getting extended info for user: $userId")
        return mapOf("logging_enabled" to true)
    }
    
    // Override metody z logowaniem
    suspend fun allowAction(
        userId: String, 
        action: String, 
        quotaKey: String?, 
        flagKey: String?
    ): AccessResult {
        logger("Checking access for user: $userId, action: $action")
        val result = guard.allowAction(userId, action, quotaKey, flagKey)
        logger("Access result: ${result.allowed}, reason: ${result.reason}")
        return result
    }
}
