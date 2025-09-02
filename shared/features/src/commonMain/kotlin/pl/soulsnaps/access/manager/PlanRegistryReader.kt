package pl.soulsnaps.access.manager

import pl.soulsnaps.access.model.PlanType

/**
 * Interfejs do odczytu planów użytkowników
 */
interface PlanRegistryReader {
    
    /**
     * Pobierz plan użytkownika
     */
    suspend fun getPlan(userId: String): PlanDefinition?
    
    /**
     * Pobierz plan po typie
     */
    fun getPlanByType(type: PlanType): PlanDefinition
    
    /**
     * Sprawdź czy użytkownik ma plan
     */
    suspend fun hasPlan(userId: String): Boolean
    
    /**
     * Pobierz zalecany plan dla akcji
     */
    fun getRecommendedPlanForAction(action: String): PlanType?
    
    /**
     * Pobierz wszystkie dostępne plany
     */
    fun getAllPlans(): List<PlanDefinition>
}

/**
 * Implementacja PlanRegistryReader
 */
class PlanRegistryReaderImpl : PlanRegistryReader {
    
    override suspend fun getPlan(userId: String): PlanDefinition? {
        // TODO: Implementacja pobierania planu z bazy danych
        // Na razie zwracamy domyślny plan
        return DefaultPlans.GUEST
    }
    
    override fun getPlanByType(type: PlanType): PlanDefinition {
        return DefaultPlans.getPlan(type)
    }
    
    override suspend fun hasPlan(userId: String): Boolean {
        return getPlan(userId) != null
    }
    
    override fun getRecommendedPlanForAction(action: String): PlanType? {
        return when {
            action.startsWith("ai.") -> PlanType.PREMIUM_USER
            action.startsWith("export.") -> PlanType.FREE_USER
            action.startsWith("memory.") -> PlanType.GUEST
            else -> null
        }
    }
    
    override fun getAllPlans(): List<PlanDefinition> {
        return DefaultPlans.getAllPlans()
    }
}



