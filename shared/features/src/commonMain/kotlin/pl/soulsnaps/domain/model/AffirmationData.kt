package pl.soulsnaps.domain.model

import pl.soulsnaps.utils.getCurrentTimeMillis

/**
 * Affirmation data model
 */
data class AffirmationData(
    val id: String? = null,
    val content: String,
    val createdAtIso: String = getCurrentTimeMillis().toString(),
    val upgradedByAi: Boolean = false,
    val affirmationRequested: Boolean = true,
    val memoryId: String? = null,
    val emotion: String? = null,
    val intensity: Float? = null,
    val timeOfDay: String? = null,
    val location: String? = null,
    val tags: List<String> = emptyList(),
    val version: AffirmationVersion = AffirmationVersion.RULE,
    val generationTimeMs: Long = 0L
)

/**
 * Affirmation generation version
 */
enum class AffirmationVersion {
    RULE,      // Offline rule-based generation
    AI         // AI-enhanced generation
}

/**
 * Affirmation generation request
 */
data class AffirmationRequest(
    val emotion: String?,
    val intensity: Float?,
    val timeOfDay: String?,
    val location: String?,
    val tags: List<String>,
    val memoryId: String? = null
)

/**
 * Affirmation generation result
 */
data class AffirmationResult(
    val content: String,
    val version: AffirmationVersion,
    val generationTimeMs: Long,
    val success: Boolean,
    val error: String? = null
)

/**
 * Offline affirmation generator rules
 */
object AffirmationRules {
    
    private val emotionTemplates = mapOf(
        "Radość" to listOf(
            "Pozwalam sobie czuć tę radość i wdzięczność.",
            "Cieszę się tym momentem i doceniam go.",
            "Radość jest moim naturalnym stanem."
        ),
        "Spokój" to listOf(
            "Oddycham spokojnie; to dobry moment na życzliwość dla siebie.",
            "Jestem w harmonii z tym, co jest.",
            "Spokój płynie przez moje ciało i umysł."
        ),
        "Smutek" to listOf(
            "Jestem dla siebie delikatny — to minie.",
            "Pozwalam sobie czuć to, co czuję.",
            "Smutek jest częścią życia i ma swój czas."
        ),
        "Złość" to listOf(
            "Uznaję swoje uczucia i kieruję energię konstruktywnie.",
            "Jestem silny i potrafię poradzić sobie z tym.",
            "Złość pokazuje mi, co jest dla mnie ważne."
        ),
        "Strach" to listOf(
            "Jestem bezpieczny i mam w sobie siłę.",
            "Strach nie musi mnie kontrolować.",
            "Ufam sobie i swoim instynktom."
        ),
        "Wstyd" to listOf(
            "Jestem godny miłości i akceptacji.",
            "Pozwalam sobie być człowiekiem z niedoskonałościami.",
            "Wstyd nie definiuje mojej wartości."
        ),
        "Wina" to listOf(
            "Uczę się z tego doświadczenia i wybaczam sobie.",
            "Jestem człowiekiem i mam prawo do błędów.",
            "Wybaczam sobie i idę dalej z mądrością."
        ),
        "Wstręt" to listOf(
            "Szanuję swoje granice i potrzeby.",
            "Jestem wdzięczny za to, co mnie chroni.",
            "Moje instynkty pomagają mi dbać o siebie."
        )
    )
    
    private val timeOfDayTemplates = mapOf(
        "Poranek" to listOf(
            "To nowy dzień pełen możliwości.",
            "Zaczynam dzień z wdzięcznością.",
            "Poranek przynosi mi świeżość i nadzieję."
        ),
        "Południe" to listOf(
            "Energia dnia wspiera moje cele.",
            "Jestem w pełni sił i gotowy na wyzwania.",
            "Południe przypomina mi o mojej sile."
        ),
        "Wieczór" to listOf(
            "Wieczór to czas na refleksję i spokój.",
            "Zamykam dzień z wdzięcznością za to, co było.",
            "Wieczór przynosi mi ukojenie i odpoczynek."
        ),
        "Noc" to listOf(
            "Noc otula mnie spokojem i bezpieczeństwem.",
            "Jestem bezpieczny w ciemności.",
            "Noc daje mi czas na regenerację."
        )
    )
    
    private val neutralTemplates = listOf(
        "Jestem obecny tu i teraz.",
        "Akceptuję siebie takim, jakim jestem.",
        "Jestem wdzięczny za ten moment.",
        "Ufam procesowi życia.",
        "Jestem wystarczający taki, jaki jestem.",
        "Pozwalam sobie być autentycznym.",
        "Jestem w harmonii z sobą.",
        "Doceniam to, co mam w tym momencie."
    )
    
    /**
     * Generate affirmation based on rules
     */
    fun generateAffirmation(request: AffirmationRequest): AffirmationResult {
        val startTime = getCurrentTimeMillis()
        
        return try {
            val content = generateContent(request)
            val generationTime = getCurrentTimeMillis() - startTime
            
            AffirmationResult(
                content = content,
                version = AffirmationVersion.RULE,
                generationTimeMs = generationTime,
                success = true
            )
        } catch (e: Exception) {
            val generationTime = getCurrentTimeMillis() - startTime
            AffirmationResult(
                content = "Jestem obecny tu i teraz.",
                version = AffirmationVersion.RULE,
                generationTimeMs = generationTime,
                success = false,
                error = e.message
            )
        }
    }
    
    private fun generateContent(request: AffirmationRequest): String {
        // Try emotion-based generation first
        if (!request.emotion.isNullOrEmpty()) {
            val emotionTemplates = emotionTemplates[request.emotion]
            if (!emotionTemplates.isNullOrEmpty()) {
                val selectedTemplate = emotionTemplates.random()
                return selectedTemplate
            }
        }
        
        // Try time-of-day based generation
        if (!request.timeOfDay.isNullOrEmpty()) {
            val timeTemplates = timeOfDayTemplates[request.timeOfDay]
            if (!timeTemplates.isNullOrEmpty()) {
                val selectedTemplate = timeTemplates.random()
                return selectedTemplate
            }
        }
        
        // Fall back to neutral templates
        return neutralTemplates.random()
    }
    
    /**
     * Validate affirmation content
     */
    fun validateContent(content: String): Boolean {
        if (content.length > 120) return false
        if (content.count { it == '!' } > 2) return false
        if (content.contains("muszę") || content.contains("powinienem")) return false
        return true
    }
}
