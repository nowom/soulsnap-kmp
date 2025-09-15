package pl.soulsnaps.access.guard

import pl.soulsnaps.access.manager.PlanRegistryReader
import pl.soulsnaps.access.guard.QuotaInfo

/**
 * CapacityGuard - specjalizuje się w sprawdzaniu limitów pojemności
 * Rozszerza AccessGuard o funkcjonalność sprawdzania limitów storage, snapów, etc.
 */
class CapacityGuard(
    private val accessGuard: AccessGuard,
    private val planRegistry: PlanRegistryReader
) {
    
    /**
     * Sprawdź czy użytkownik może dodać nowy snap (sprawdza limit pojemności)
     */
    suspend fun canAddSnap(userId: String): AccessResult {
        return accessGuard.allowAction(
            userId = userId,
            action = "memory.create",
            quotaKey = "snaps.capacity"
        )
    }
    
    /**
     * Sprawdź czy użytkownik może dodać snap o określonym rozmiarze
     */
    suspend fun canAddSnapWithSize(userId: String, sizeInMB: Int): AccessResult {
        // Sprawdź limit snapów
        val snapResult = accessGuard.allowAction(
            userId = userId,
            action = "memory.create",
            quotaKey = "snaps.capacity"
        )
        
        if (!snapResult.allowed) return snapResult
        
        // Sprawdź limit storage
        val storageResult = accessGuard.allowAction(
            userId = userId,
            action = "memory.create",
            quotaKey = "storage.gb"
        )
        
        if (!storageResult.allowed) return storageResult
        
        // Sprawdź czy rozmiar nie przekracza dostępnego miejsca
        val quotaInfo = accessGuard.getQuotaInfo(userId, "storage.gb")
        val availableStorageMB = (quotaInfo?.limit ?: 0) * 1024 // Convert GB to MB
        
        if (sizeInMB > availableStorageMB) {
            return AccessResult(
                allowed = false,
                reason = DenyReason.QUOTA_EXCEEDED,
                message = "Rozmiar pliku przekracza dostępne miejsce w storage",
                quotaInfo = quotaInfo
            )
        }
        
        return AccessResult(allowed = true)
    }
    
    /**
     * Sprawdź czy użytkownik może uruchomić analizę AI
     */
    suspend fun canRunAIAnalysis(userId: String): AccessResult {
        return accessGuard.allowAction(
            userId = userId,
            action = "analysis.run.single",
            quotaKey = "ai.daily"
        )
    }
    
    /**
     * Sprawdź czy użytkownik może uruchomić analizę wzorców
     */
    suspend fun canRunPatternAnalysis(userId: String): AccessResult {
        return accessGuard.allowAction(
            userId = userId,
            action = "analysis.run.patterns",
            quotaKey = "analysis.patterns.day"
        )
    }
    
    /**
     * Sprawdź czy użytkownik może dodać więcej wspomnień w tym miesiącu
     */
    suspend fun canAddMemory(userId: String): AccessResult {
        return accessGuard.allowAction(
            userId = userId,
            action = "memory.create",
            quotaKey = "snaps.capacity"
        )
    }
    
    /**
     * Sprawdź czy użytkownik może wykonać eksport
     */
    suspend fun canExport(userId: String): AccessResult {
        return accessGuard.allowAction(
            userId = userId,
            action = "export.pdf", // FREE_USER has export.pdf scope
            quotaKey = null // No specific quota for export.pdf
        )
    }
    
    /**
     * Sprawdź czy użytkownik może wykonać backup
     */
    suspend fun canCreateBackup(userId: String): AccessResult {
        return accessGuard.allowAction(
            userId = userId,
            action = "backup.create", // FREE_USER doesn't have this scope
            quotaKey = null
        )
    }
    
    /**
     * Pobierz informacje o pojemności użytkownika
     */
    suspend fun getCapacityInfo(userId: String): CapacityInfo {
        val snapQuota = accessGuard.getQuotaInfo(userId, "snaps.capacity")
        val storageQuota = accessGuard.getQuotaInfo(userId, "storage.gb")
        val aiQuota = accessGuard.getQuotaInfo(userId, "ai.daily")
        
        return CapacityInfo(
            snaps = snapQuota,
            storage = storageQuota,
            aiAnalysis = aiQuota,
            memories = snapQuota
        )
    }
    
    /**
     * Sprawdź czy użytkownik może wykonać akcję z określonym rozmiarem pliku
     */
    suspend fun canPerformActionWithFileSize(
        userId: String,
        action: String,
        fileSizeInMB: Int
    ): AccessResult {
        // Sprawdź podstawowe uprawnienia
        val basicResult = accessGuard.canPerformAction(userId, action)
        if (!basicResult.allowed) return basicResult
        
        // Sprawdź limit storage jeśli akcja dotyczy plików
        if (action.contains("memory") || action.contains("export") || action.contains("backup")) {
            val storageQuota = accessGuard.getQuotaInfo(userId, "storage.gb")
            val availableStorageMB = (storageQuota?.limit ?: 0) * 1024
            
            if (fileSizeInMB > availableStorageMB) {
                return AccessResult(
                    allowed = false,
                    reason = DenyReason.QUOTA_EXCEEDED,
                    message = "Rozmiar pliku przekracza dostępne miejsce w storage",
                    quotaInfo = storageQuota
                )
            }
        }
        
        return AccessResult(allowed = true)
    }
    
    /**
     * Pobierz rekomendację upgrade'u na podstawie aktualnego użycia
     */
    suspend fun getUpgradeRecommendation(userId: String): UpgradeRecommendation {
        val capacityInfo = getCapacityInfo(userId)
        val recommendations = mutableListOf<String>()
        
        // Sprawdź różne limity i dodaj rekomendacje
        capacityInfo.snaps?.let { quota ->
            if (quota.current >= quota.limit * 0.8) { // 80% wykorzystania
                recommendations.add("Limit snapów prawie wyczerpany (${quota.current}/${quota.limit})")
            }
        }
        
        capacityInfo.storage?.let { quota ->
            if (quota.current >= quota.limit * 0.8) {
                recommendations.add("Storage prawie pełny (${quota.current}/${quota.limit} GB)")
            }
        }
        
        capacityInfo.aiAnalysis?.let { quota ->
            if (quota.current >= quota.limit * 0.8) {
                recommendations.add("Limit analiz AI prawie wyczerpany (${quota.current}/${quota.limit})")
            }
        }
        
        capacityInfo.memories?.let { quota ->
            if (quota.current >= quota.limit * 0.8) {
                recommendations.add("Limit wspomnień miesięczny prawie wyczerpany (${quota.current}/${quota.limit})")
            }
        }
        
        val recommendedPlan = when {
            recommendations.size >= 3 -> "PREMIUM_USER"
            recommendations.size >= 2 -> "FREE_USER"
            else -> null
        }
        
        return UpgradeRecommendation(
            recommendations = recommendations,
            recommendedPlan = recommendedPlan,
            urgency = when {
                recommendations.size >= 3 -> UpgradeUrgency.HIGH
                recommendations.size >= 2 -> UpgradeUrgency.MEDIUM
                else -> UpgradeUrgency.LOW
            }
        )
    }
}

/**
 * Informacje o pojemności użytkownika
 */
data class CapacityInfo(
    val snaps: QuotaInfo?,
    val storage: QuotaInfo?,
    val aiAnalysis: QuotaInfo?,
    val memories: QuotaInfo?
)

/**
 * Rekomendacja upgrade'u
 */
data class UpgradeRecommendation(
    val recommendations: List<String>,
    val recommendedPlan: String?,
    val urgency: UpgradeUrgency
)

/**
 * Poziom pilności upgrade'u
 */
enum class UpgradeUrgency {
    LOW, MEDIUM, HIGH
}
