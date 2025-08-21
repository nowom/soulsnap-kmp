package pl.soulsnaps.features.auth.mvp.guard

import pl.soulsnaps.features.auth.mvp.guard.model.PlanRestriction

/**
 * Przykład użycia CapacityGuard
 * Pokazuje jak używać CapacityGuard do sprawdzania limitów pojemności
 */
object CapacityGuardExample {
    
    /**
     * Przykład sprawdzania czy użytkownik może dodać nowy snap
     */
    suspend fun exampleCheckSnapCapacity() {
        val capacityGuard = GuardFactory.createCapacityGuard()
        val userId = "user123"
        
        // Sprawdź czy użytkownik może dodać snap
        val result = capacityGuard.canAddSnap(userId)
        
        when {
            result.allowed -> {
                println("✅ Użytkownik może dodać nowy snap")
            }
            result.reason == DenyReason.QUOTA_EXCEEDED -> {
                println("❌ Limit snapów wyczerpany")
                println("Pozostało: ${result.quotaInfo?.let { it.limit - it.current }} snapów")
                
                // Pokaż rekomendację upgrade'u
                val recommendation = capacityGuard.getUpgradeRecommendation(userId)
                if (recommendation.recommendedPlan != null) {
                    println("💡 Rekomendowany plan: ${recommendation.recommendedPlan}")
                }
            }
            else -> {
                println("❌ Brak uprawnień: ${result.message}")
            }
        }
    }
    
    /**
     * Przykład sprawdzania pojemności z rozmiarem pliku
     */
    suspend fun exampleCheckFileSizeCapacity() {
        val capacityGuard = GuardFactory.createCapacityGuard()
        val userId = "user123"
        val fileSizeMB = 150 // 150MB plik
        
        // Sprawdź czy użytkownik może dodać plik o tym rozmiarze
        val result = capacityGuard.canAddSnapWithSize(userId, fileSizeMB)
        
        when {
            result.allowed -> {
                println("✅ Użytkownik może dodać plik o rozmiarze ${fileSizeMB}MB")
            }
            result.reason == DenyReason.QUOTA_EXCEEDED -> {
                println("❌ Plik przekracza dostępne miejsce")
                println("Rozmiar pliku: ${fileSizeMB}MB")
                println("Dostępne miejsce: ${result.quotaInfo?.limit}GB")
                
                // Pokaż rekomendację upgrade'u
                val recommendation = capacityGuard.getUpgradeRecommendation(userId)
                if (recommendation.recommendedPlan != null) {
                    println("💡 Rekomendowany plan: ${recommendation.recommendedPlan}")
                }
            }
            else -> {
                println("❌ Problem z uprawnieniami: ${result.message}")
            }
        }
    }
    
    /**
     * Przykład sprawdzania analizy AI
     */
    suspend fun exampleCheckAIAnalysis() {
        val capacityGuard = GuardFactory.createCapacityGuard()
        val userId = "user123"
        
        // Sprawdź czy użytkownik może uruchomić analizę AI
        val result = capacityGuard.canRunAIAnalysis(userId)
        
        when {
            result.allowed -> {
                println("✅ Użytkownik może uruchomić analizę AI")
                
                // Pokaż pozostałe analizy
                val quotaInfo = capacityGuard.getCapacityInfo(userId)
                quotaInfo.aiAnalysis?.let { ai ->
                    println("Pozostało analiz AI: ${ai.limit - ai.current}")
                }
            }
            result.reason == DenyReason.QUOTA_EXCEEDED -> {
                println("❌ Limit analiz AI wyczerpany")
                println("Reset o 00:00")
                
                // Pokaż rekomendację upgrade'u
                val recommendation = capacityGuard.getUpgradeRecommendation(userId)
                if (recommendation.recommendedPlan != null) {
                    println("💡 Rekomendowany plan: ${recommendation.recommendedPlan}")
                }
            }
            else -> {
                println("❌ Brak uprawnień do analizy AI: ${result.message}")
            }
        }
    }
    
    /**
     * Przykład sprawdzania wszystkich limitów pojemności
     */
    suspend fun exampleCheckAllCapacity() {
        val capacityGuard = GuardFactory.createCapacityGuard()
        val userId = "user123"
        
        // Pobierz informacje o wszystkich limitach
        val capacityInfo = capacityGuard.getCapacityInfo(userId)
        
        println("📊 Status pojemności użytkownika $userId:")
        
        capacityInfo.snaps?.let { quota ->
            val remaining = quota.limit - quota.current
            val percentage = (quota.current.toFloat() / quota.limit * 100).toInt()
            println("📸 Snaps: $remaining/$quota.limit pozostało ($percentage% wykorzystane)")
        }
        
        capacityInfo.storage?.let { quota ->
            val remaining = quota.limit - quota.current
            val percentage = (quota.current.toFloat() / quota.limit * 100).toInt()
            println("💾 Storage: $remaining/$quota.limit GB pozostało ($percentage% wykorzystane)")
        }
        
        capacityInfo.aiAnalysis?.let { quota ->
            val remaining = quota.limit - quota.current
            val percentage = (quota.current.toFloat() / quota.limit * 100).toInt()
            println("🤖 AI Analysis: $remaining/$quota.limit pozostało ($percentage% wykorzystane)")
        }
        
        capacityInfo.memories?.let { quota ->
            val remaining = quota.limit - quota.current
            val percentage = (quota.current.toFloat() / quota.limit * 100).toInt()
            println("🧠 Memories: $remaining/$quota.limit pozostało ($percentage% wykorzystane)")
        }
        
        // Pobierz rekomendację upgrade'u
        val recommendation = capacityGuard.getUpgradeRecommendation(userId)
        if (recommendation.recommendations.isNotEmpty()) {
            println("\n💡 Rekomendacje:")
            recommendation.recommendations.forEach { rec ->
                println("   • $rec")
            }
            
            if (recommendation.recommendedPlan != null) {
                println("   • Rekomendowany plan: ${recommendation.recommendedPlan}")
            }
            
            println("   • Pilność: ${recommendation.urgency}")
        }
    }
    
    /**
     * Przykład integracji z PaywallTrigger
     */
    suspend fun exampleIntegrationWithPaywallTrigger() {
        val capacityGuard = GuardFactory.createCapacityGuard()
        val accessGuard = GuardFactory.createDefaultGuard()
        val paywallTrigger = PaywallTrigger(accessGuard, DefaultPlans)
        val userId = "user123"
        
        // Sprawdź czy użytkownik może dodać snap
        val snapResult = capacityGuard.canAddSnap(userId)
        
        if (!snapResult.allowed && snapResult.reason == DenyReason.QUOTA_EXCEEDED) {
            // Użytkownik przekroczył limit - wyzwól paywall
            paywallTrigger.checkCapacityLimit(userId, 50) { restriction ->
                println("🚨 Paywall wyzwolony!")
                println("Typ: ${restriction::class.simpleName}")
                when (restriction) {
                    is PlanRestriction.SnapsCapacity -> {
                        println("Limit snapów: ${restriction.current}/${restriction.limit}")
                        println("Wiadomość: ${restriction.message}")
                    }
                    else -> println("Ograniczenie: $restriction")
                }
            }
            
            // Pokaż rekomendację upgrade'u
            val recommendation = capacityGuard.getUpgradeRecommendation(userId)
            if (recommendation.recommendedPlan != null) {
                println("💡 Rekomendowany plan: ${recommendation.recommendedPlan}")
            }
        }
    }
    
    /**
     * Przykład użycia w ViewModel lub UseCase
     */
    suspend fun exampleUsageInViewModel(userId: String, fileSizeMB: Int): Boolean {
        val capacityGuard = GuardFactory.createCapacityGuard()
        
        // Sprawdź czy użytkownik może dodać plik
        val result = capacityGuard.canAddSnapWithSize(userId, fileSizeMB)
        
        return when {
            result.allowed -> {
                // Użytkownik może dodać plik
                true
            }
            result.reason == DenyReason.QUOTA_EXCEEDED -> {
                // Użytkownik przekroczył limit - pokaż paywall
                // W rzeczywistej aplikacji tu byłby navigation do paywall
                println("Limit przekroczony: ${result.message}")
                false
            }
            else -> {
                // Inny problem z uprawnieniami
                println("Błąd uprawnień: ${result.message}")
                false
            }
        }
    }
}
