package pl.soulsnaps.features.auth.mvp.guard

import pl.soulsnaps.features.auth.mvp.guard.model.AppAction
import pl.soulsnaps.features.auth.mvp.guard.model.AppFeature
import pl.soulsnaps.features.auth.mvp.guard.model.PlanRestriction

/**
 * PaywallTriggerExample - Przykłady użycia PaywallTrigger w praktyce
 * Pokazuje jak implementować paywall w różnych scenariuszach aplikacji
 */
class PaywallTriggerExample(
    private val paywallTrigger: PaywallTrigger
) {
    
    /**
     * Przykład 1: Sprawdzanie przed utworzeniem nowego Snap'a
     */
    suspend fun checkBeforeCreatingSnap(
        userId: String,
        currentSnapCount: Int,
        onRestricted: (PlanRestriction) -> Unit
    ): Boolean {
        return paywallTrigger.checkCapacityLimit(
            userId = userId,
            currentCount = currentSnapCount,
            onRestricted = onRestricted
        )
    }
    
    /**
     * Przykład 2: Sprawdzanie przed użyciem AI
     */
    suspend fun checkBeforeUsingAI(
        userId: String,
        aiUsageToday: Int,
        onRestricted: (PlanRestriction) -> Unit
    ): Boolean {
        return paywallTrigger.checkAIDailyLimit(
            userId = userId,
            usedToday = aiUsageToday,
            onRestricted = onRestricted
        )
    }
    
    /**
     * Przykład 3: Sprawdzanie przed eksportem wideo
     */
    suspend fun checkBeforeVideoExport(
        userId: String,
        onRestricted: (PlanRestriction) -> Unit
    ): Boolean {
        return paywallTrigger.checkFeatureAccess(
            userId = userId,
            feature = AppFeature.EXPORT_VIDEO,
            onRestricted = onRestricted
        )
    }
    
    /**
     * Przykład 4: Sprawdzanie przed backup'em do chmury
     */
    suspend fun checkBeforeCloudBackup(
        userId: String,
        onRestricted: (PlanRestriction) -> Unit
    ): Boolean {
        return paywallTrigger.checkFeatureAccess(
            userId = userId,
            feature = AppFeature.BACKUP_CLOUD,
            onRestricted = onRestricted
        )
    }
    
    /**
     * Przykład 5: Sprawdzanie przed użyciem zaawansowanych filtrów
     */
    suspend fun checkBeforeAdvancedFilters(
        userId: String,
        onRestricted: (PlanRestriction) -> Unit
    ): Boolean {
        return paywallTrigger.checkFeatureAccess(
            userId = userId,
            feature = AppFeature.FILTERS_ADVANCED,
            onRestricted = onRestricted
        )
    }
    
    /**
     * Przykład 6: Sprawdzanie przed dodaniem audio do Snap'a
     */
    suspend fun checkBeforeAudioAttach(
        userId: String,
        onRestricted: (PlanRestriction) -> Unit
    ): Boolean {
        return paywallTrigger.checkFeatureAccess(
            userId = userId,
            feature = AppFeature.AUDIO_ATTACH,
            onRestricted = onRestricted
        )
    }
    
    /**
     * Przykład 7: Sprawdzanie przed użyciem AI Insights
     */
    suspend fun checkBeforeAIInsights(
        userId: String,
        onRestricted: (PlanRestriction) -> Unit
    ): Boolean {
        return paywallTrigger.checkFeatureAccess(
            userId = userId,
            feature = AppFeature.AI_INSIGHTS,
            onRestricted = onRestricted
        )
    }
    
    /**
     * Przykład 8: Sprawdzanie przed użyciem zaawansowanej mapy
     */
    suspend fun checkBeforeAdvancedMap(
        userId: String,
        onRestricted: (PlanRestriction) -> Unit
    ): Boolean {
        return paywallTrigger.checkFeatureAccess(
            userId = userId,
            feature = AppFeature.MAP_ADVANCED,
            onRestricted = onRestricted
        )
    }
    
    /**
     * Przykład 9: Sprawdzanie przed udostępnieniem linkiem
     */
    suspend fun checkBeforeShareLink(
        userId: String,
        onRestricted: (PlanRestriction) -> Unit
    ): Boolean {
        return paywallTrigger.checkFeatureAccess(
            userId = userId,
            feature = AppFeature.SHARE_LINK,
            onRestricted = onRestricted
        )
    }
    
    /**
     * Przykład 10: Sprawdzanie przed użyciem API
     */
    suspend fun checkBeforeAPIAccess(
        userId: String,
        onRestricted: (PlanRestriction) -> Unit
    ): Boolean {
        return paywallTrigger.checkFeatureAccess(
            userId = userId,
            feature = AppFeature.API_ACCESS,
            onRestricted = onRestricted
        )
    }
    
    /**
     * Przykład 11: Kompleksowe sprawdzanie przed utworzeniem Snap'a z AI
     */
    suspend fun checkBeforeCreatingSnapWithAI(
        userId: String,
        currentSnapCount: Int,
        aiUsageToday: Int,
        onRestricted: (PlanRestriction) -> Unit
    ): Boolean {
        // Sprawdź limit Snapów
        val canCreateSnap = paywallTrigger.checkCapacityLimit(
            userId = userId,
            currentCount = currentSnapCount,
            onRestricted = onRestricted
        )
        
        if (!canCreateSnap) return false
        
        // Sprawdź limit AI
        val canUseAI = paywallTrigger.checkAIDailyLimit(
            userId = userId,
            usedToday = aiUsageToday,
            onRestricted = onRestricted
        )
        
        return canUseAI
    }
    
    /**
     * Przykład 12: Sprawdzanie przed eksportem danych
     */
    suspend fun checkBeforeDataExport(
        userId: String,
        exportFormat: String,
        onRestricted: (PlanRestriction) -> Unit
    ): Boolean {
        val action = when (exportFormat.lowercase()) {
            "video" -> AppAction.ExportVideo()
            "pdf" -> AppAction.ExportData("PDF")
            else -> AppAction.ExportData(exportFormat)
        }
        
        return paywallTrigger.checkAndTrigger(
            userId = userId,
            action = action,
            onRestricted = onRestricted
        )
    }
}

/**
 * Przykład użycia w ViewModel lub UseCase
 */
class PaywallUsageExample {
    
    suspend fun exampleUsage(paywallTrigger: PaywallTrigger) {
        val userId = "user123"
        
        // Sprawdź przed utworzeniem Snap'a
        val canCreateSnap = paywallTrigger.checkCapacityLimit(
            userId = userId,
            currentCount = 45, // Przykładowa liczba
            onRestricted = { restriction ->
                when (restriction) {
                    is PlanRestriction.SnapsCapacity -> {
                        // Pokaż modal paywalla
                        showPaywallModal(
                            title = "Osiągnięto limit Snapów",
                            message = restriction.message,
                            upgradeRequired = true
                        )
                    }
                    else -> {
                        // Obsłuż inne typy restrykcji
                        val message = when (restriction) {
                            is PlanRestriction.GenericRestriction -> restriction.message
                            is PlanRestriction.FeatureRestriction -> restriction.message
                            is PlanRestriction.SnapsCapacity -> restriction.message
                            is PlanRestriction.AIDailyLimit -> restriction.message
                            is PlanRestriction.StorageLimit -> restriction.message
                        }
                        showGenericRestriction(message)
                    }
                }
            }
        )
        
        if (canCreateSnap) {
            // Kontynuuj tworzenie Snap'a
            createSnap()
        }
        
        // Sprawdź przed użyciem AI
        val canUseAI = paywallTrigger.checkAIDailyLimit(
            userId = userId,
            usedToday = 4, // Przykładowa liczba
            onRestricted = { restriction ->
                when (restriction) {
                    is PlanRestriction.AIDailyLimit -> {
                        showPaywallModal(
                            title = "Limit AI wykorzystany",
                            message = restriction.message,
                            upgradeRequired = true
                        )
                    }
                    else -> {
                        // Obsłuż inne typy restrykcji
                        val message = when (restriction) {
                            is PlanRestriction.GenericRestriction -> restriction.message
                            is PlanRestriction.FeatureRestriction -> restriction.message
                            is PlanRestriction.SnapsCapacity -> restriction.message
                            is PlanRestriction.AIDailyLimit -> restriction.message
                            is PlanRestriction.StorageLimit -> restriction.message
                        }
                        showGenericRestriction(message)
                    }
                }
            }
        )
        
        if (canUseAI) {
            // Kontynuuj użycie AI
            useAI()
        }
    }
    
    private fun showPaywallModal(title: String, message: String, upgradeRequired: Boolean) {
        // Implementacja wyświetlania modala paywalla
        println("Paywall Modal: $title - $message (Upgrade: $upgradeRequired)")
    }
    
    private fun showGenericRestriction(message: String) {
        // Implementacja wyświetlania ogólnej restrykcji
        println("Restriction: $message")
    }
    
    private fun createSnap() {
        // Implementacja tworzenia Snap'a
        println("Creating snap...")
    }
    
    private fun useAI() {
        // Implementacja użycia AI
        println("Using AI...")
    }
}
