package pl.soulsnaps.domain.service

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import pl.soulsnaps.domain.model.AffirmationData
import pl.soulsnaps.domain.model.AffirmationRequest
import pl.soulsnaps.domain.model.AffirmationResult
import pl.soulsnaps.domain.model.AffirmationVersion
import pl.soulsnaps.utils.getCurrentTimeMillis

/**
 * Service for generating and managing affirmations
 */
interface AffirmationService {
    /**
     * Generate affirmation based on request
     */
    suspend fun generateAffirmation(request: AffirmationRequest): AffirmationResult
    
    /**
     * Generate AI-upgraded affirmation
     */
    suspend fun generateAiUpgrade(originalAffirmation: AffirmationData): AffirmationResult
    
    /**
     * Save affirmation to storage
     */
    suspend fun saveAffirmation(affirmation: AffirmationData): String
    
    /**
     * Get affirmation by ID
     */
    suspend fun getAffirmation(id: String): AffirmationData?
    
    /**
     * Get affirmations for memory
     */
    suspend fun getAffirmationsForMemory(memoryId: String): List<AffirmationData>
    
    /**
     * Get affirmation status flow
     */
    fun getAffirmationStatusFlow(): StateFlow<AffirmationStatus>
    
    /**
     * Check if AI upgrade is available
     */
    suspend fun isAiUpgradeAvailable(): Boolean
}

/**
 * Affirmation status for UI updates
 */
data class AffirmationStatus(
    val isGenerating: Boolean = false,
    val isUpgrading: Boolean = false,
    val lastGenerated: AffirmationData? = null,
    val lastUpgraded: AffirmationData? = null,
    val error: String? = null
)

/**
 * Implementation of AffirmationService
 */
class AffirmationServiceImpl : AffirmationService {
    
    private val _status = MutableStateFlow(AffirmationStatus())
    private val affirmations = mutableMapOf<String, AffirmationData>()
    
    override suspend fun generateAffirmation(request: AffirmationRequest): AffirmationResult {
        _status.value = _status.value.copy(isGenerating = true, error = null)
        
        return try {
            val result = pl.soulsnaps.domain.model.AffirmationRules.generateAffirmation(request)
            
            if (result.success) {
                val affirmation = AffirmationData(
                    content = result.content,
                    upgradedByAi = false,
                    affirmationRequested = true,
                    memoryId = request.memoryId,
                    emotion = request.emotion,
                    intensity = request.intensity,
                    timeOfDay = request.timeOfDay,
                    location = request.location,
                    tags = request.tags,
                    version = result.version,
                    generationTimeMs = result.generationTimeMs
                )
                
                val id = saveAffirmation(affirmation)
                val savedAffirmation = affirmation.copy(id = id)
                
                _status.value = _status.value.copy(
                    isGenerating = false,
                    lastGenerated = savedAffirmation
                )
                
                result
            } else {
                _status.value = _status.value.copy(
                    isGenerating = false,
                    error = result.error
                )
                result
            }
        } catch (e: Exception) {
            _status.value = _status.value.copy(
                isGenerating = false,
                error = e.message
            )
            AffirmationResult(
                content = "Jestem obecny tu i teraz.",
                version = AffirmationVersion.RULE,
                generationTimeMs = 0L,
                success = false,
                error = e.message
            )
        }
    }
    
    override suspend fun generateAiUpgrade(originalAffirmation: AffirmationData): AffirmationResult {
        _status.value = _status.value.copy(isUpgrading = true, error = null)
        
        return try {
            // Simulate AI upgrade (in real implementation, this would call AI service)
            val upgradedContent = upgradeContentWithAi(originalAffirmation)
            
            val result = AffirmationResult(
                content = upgradedContent,
                version = AffirmationVersion.AI,
                generationTimeMs = 1000L, // Simulate AI processing time
                success = true
            )
            
            if (result.success) {
                val upgradedAffirmation = originalAffirmation.copy(
                    content = result.content,
                    upgradedByAi = true,
                    version = result.version,
                    generationTimeMs = result.generationTimeMs
                )
                
                val id = saveAffirmation(upgradedAffirmation)
                val savedAffirmation = upgradedAffirmation.copy(id = id)
                
                _status.value = _status.value.copy(
                    isUpgrading = false,
                    lastUpgraded = savedAffirmation
                )
            }
            
            result
        } catch (e: Exception) {
            _status.value = _status.value.copy(
                isUpgrading = false,
                error = e.message
            )
            AffirmationResult(
                content = originalAffirmation.content,
                version = originalAffirmation.version,
                generationTimeMs = 0L,
                success = false,
                error = e.message
            )
        }
    }
    
    override suspend fun saveAffirmation(affirmation: AffirmationData): String {
        val id = "aff_${getCurrentTimeMillis()}_${(0..999).random()}"
        val savedAffirmation = affirmation.copy(id = id)
        affirmations[id] = savedAffirmation
        return id
    }
    
    override suspend fun getAffirmation(id: String): AffirmationData? {
        return affirmations[id]
    }
    
    override suspend fun getAffirmationsForMemory(memoryId: String): List<AffirmationData> {
        return affirmations.values.filter { it.memoryId == memoryId }
    }
    
    override fun getAffirmationStatusFlow(): StateFlow<AffirmationStatus> = _status
    
    override suspend fun isAiUpgradeAvailable(): Boolean {
        // In real implementation, this would check network connectivity
        return true
    }
    
    /**
     * Simulate AI upgrade of content
     */
    private fun upgradeContentWithAi(original: AffirmationData): String {
        val baseContent = original.content
        
        // Simple AI upgrade simulation
        val upgrades = mapOf(
            "Jestem obecny tu i teraz." to "Jestem w pełni obecny w tym momencie, doceniając każdy oddech i każdą chwilę.",
            "Pozwalam sobie czuć tę radość i wdzięczność." to "Pozwalam sobie w pełni doświadczać tej radości, która płynie z głębi mojego serca i wypełnia mnie wdzięcznością.",
            "Oddycham spokojnie; to dobry moment na życzliwość dla siebie." to "Z każdym oddechem wpuszczam spokój do mojego ciała, przypominając sobie, że zasługuję na życzliwość i troskę.",
            "Jestem dla siebie delikatny — to minie." to "Jestem dla siebie delikatny i cierpliwy, wiedząc, że trudne chwile są przejściowe i przynoszą mi mądrość."
        )
        
        return upgrades[baseContent] ?: baseContent
    }
}
