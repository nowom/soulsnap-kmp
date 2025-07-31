package pl.soulsnaps.domain.model

import androidx.compose.ui.graphics.Color

/**
 * Repository containing all emotion data for Plutchik's Wheel of Emotions.
 * Provides access to basic emotions, complex emotions (diads), and utility functions.
 */
object EmotionData {
    
    // Basic Emotions with all intensity levels
    val emotionsCategory = mapOf(
        // JOY emotions
        "JOY_HIGH" to Emotion(
            id = "JOY_HIGH",
            name = "Ekstaza",
            emotionCategory = EmotionCategory.JOY,
            intensity = EmotionIntensity.HIGH,
            description = "Intensywna radość i euforia.",
            examples = listOf("Wygrana w loterii.", "Ślub z ukochaną osobą."),
            color = EmotionColors.JOY.darker(0.2f)
        ),
        "JOY_MEDIUM" to Emotion(
            id = "JOY_MEDIUM",
            name = "Radość",
            emotionCategory = EmotionCategory.JOY,
            intensity = EmotionIntensity.MEDIUM,
            description = "Pozytywne uczucie szczęścia i zadowolenia.",
            examples = listOf("Spotkanie z przyjaciółmi.", "Dobra ocena w szkole."),
            color = EmotionColors.JOY
        ),
        "JOY_LOW" to Emotion(
            id = "JOY_LOW",
            name = "Błogość",
            emotionCategory = EmotionCategory.JOY,
            intensity = EmotionIntensity.LOW,
            description = "Spokojne, łagodne uczucie zadowolenia.",
            examples = listOf("Spacer w parku.", "Czytanie ulubionej książki."),
            color = EmotionColors.JOY.lighter(0.2f)
        ),

        // TRUST emotions
        "TRUST_HIGH" to Emotion(
            id = "TRUST_HIGH",
            name = "Podziw",
            emotionCategory = EmotionCategory.TRUST,
            intensity = EmotionIntensity.HIGH,
            description = "Głęboki szacunek i uznanie.",
            examples = listOf("Podziw dla bohatera.", "Uznanie dla mistrza."),
            color = EmotionColors.TRUST.darker(0.2f)
        ),
        "TRUST_MEDIUM" to Emotion(
            id = "TRUST_MEDIUM",
            name = "Zaufanie",
            emotionCategory = EmotionCategory.TRUST,
            intensity = EmotionIntensity.MEDIUM,
            description = "Wiara w uczciwość i dobre intencje innych.",
            examples = listOf("Zawieranie umowy.", "Powołanie się na przyjaciela."),
            color = EmotionColors.TRUST
        ),
        "TRUST_LOW" to Emotion(
            id = "TRUST_LOW",
            name = "Akceptacja",
            emotionCategory = EmotionCategory.TRUST,
            intensity = EmotionIntensity.LOW,
            description = "Przyjęcie sytuacji lub osoby taką, jaka jest.",
            examples = listOf("Akceptacja różnic.", "Pogodzenie się z sytuacją."),
            color = EmotionColors.TRUST.lighter(0.2f)
        ),

        // FEAR emotions
        "FEAR_HIGH" to Emotion(
            id = "FEAR_HIGH",
            name = "Przerażenie",
            emotionCategory = EmotionCategory.FEAR,
            intensity = EmotionIntensity.HIGH,
            description = "Intensywny strach i panika.",
            examples = listOf("Terror w obliczu katastrofy.", "Paraliżujący lęk."),
            color = EmotionColors.FEAR.darker(0.2f)
        ),
        "FEAR_MEDIUM" to Emotion(
            id = "FEAR_MEDIUM",
            name = "Strach",
            emotionCategory = EmotionCategory.FEAR,
            intensity = EmotionIntensity.MEDIUM,
            description = "Uczucie zagrożenia i niepokoju.",
            examples = listOf("Strach przed ciemnością.", "Lęk wysokości."),
            color = EmotionColors.FEAR
        ),
        "FEAR_LOW" to Emotion(
            id = "FEAR_LOW",
            name = "Lęk",
            emotionCategory = EmotionCategory.FEAR,
            intensity = EmotionIntensity.LOW,
            description = "Łagodny niepokój i obawa.",
            examples = listOf("Niepokój przed egzaminem.", "Lęk przed nieznanym."),
            color = EmotionColors.FEAR.lighter(0.2f)
        ),

        // SURPRISE emotions
        "SURPRISE_HIGH" to Emotion(
            id = "SURPRISE_HIGH",
            name = "Zdumienie",
            emotionCategory = EmotionCategory.SURPRISE,
            intensity = EmotionIntensity.HIGH,
            description = "Intensywne zaskoczenie i szok.",
            examples = listOf("Nagłe odkrycie.", "Szokująca wiadomość."),
            color = EmotionColors.SURPRISE.darker(0.2f)
        ),
        "SURPRISE_MEDIUM" to Emotion(
            id = "SURPRISE_MEDIUM",
            name = "Zaskoczenie",
            emotionCategory = EmotionCategory.SURPRISE,
            intensity = EmotionIntensity.MEDIUM,
            description = "Nieoczekiwane wydarzenie lub informacja.",
            examples = listOf("Niespodziewany prezent.", "Nieoczekiwana wizyta."),
            color = EmotionColors.SURPRISE
        ),
        "SURPRISE_LOW" to Emotion(
            id = "SURPRISE_LOW",
            name = "Roztargnienie",
            emotionCategory = EmotionCategory.SURPRISE,
            intensity = EmotionIntensity.LOW,
            description = "Łagodne zaskoczenie i zainteresowanie.",
            examples = listOf("Ciekawe odkrycie.", "Nowa informacja."),
            color = EmotionColors.SURPRISE.lighter(0.2f)
        ),

        // SADNESS emotions
        "SADNESS_HIGH" to Emotion(
            id = "SADNESS_HIGH",
            name = "Żal",
            emotionCategory = EmotionCategory.SADNESS,
            intensity = EmotionIntensity.HIGH,
            description = "Intensywny smutek i rozpacz.",
            examples = listOf("Śmierć bliskiej osoby.", "Głęboka strata."),
            color = EmotionColors.SADNESS.darker(0.2f)
        ),
        "SADNESS_MEDIUM" to Emotion(
            id = "SADNESS_MEDIUM",
            name = "Smutek",
            emotionCategory = EmotionCategory.SADNESS,
            intensity = EmotionIntensity.MEDIUM,
            description = "Uczucie smutku i przygnębienia.",
            examples = listOf("Rozstanie z partnerem.", "Niepowodzenie w pracy."),
            color = EmotionColors.SADNESS
        ),
        "SADNESS_LOW" to Emotion(
            id = "SADNESS_LOW",
            name = "Melancholia",
            emotionCategory = EmotionCategory.SADNESS,
            intensity = EmotionIntensity.LOW,
            description = "Łagodny smutek i nostalgia.",
            examples = listOf("Wspomnienia z przeszłości.", "Jesienny nastrój."),
            color = EmotionColors.SADNESS.lighter(0.2f)
        ),

        // DISGUST emotions
        "DISGUST_HIGH" to Emotion(
            id = "DISGUST_HIGH",
            name = "Wstręt",
            emotionCategory = EmotionCategory.DISGUST,
            intensity = EmotionIntensity.HIGH,
            description = "Intensywny wstręt i obrzydzenie.",
            examples = listOf("Obrzydliwy widok.", "Nieprzyjemny zapach."),
            color = EmotionColors.DISGUST.darker(0.2f)
        ),
        "DISGUST_MEDIUM" to Emotion(
            id = "DISGUST_MEDIUM",
            name = "Niesmak",
            emotionCategory = EmotionCategory.DISGUST,
            intensity = EmotionIntensity.MEDIUM,
            description = "Uczucie niechęci i obrzydzenia.",
            examples = listOf("Nieprzyjemny smak.", "Obrzydliwy widok."),
            color = EmotionColors.DISGUST
        ),
        "DISGUST_LOW" to Emotion(
            id = "DISGUST_LOW",
            name = "Nuda",
            emotionCategory = EmotionCategory.DISGUST,
            intensity = EmotionIntensity.LOW,
            description = "Łagodne uczucie niechęci i znudzenia.",
            examples = listOf("Nudne zajęcia.", "Monotonne zadanie."),
            color = EmotionColors.DISGUST.lighter(0.2f)
        ),

        // ANGER emotions
        "ANGER_HIGH" to Emotion(
            id = "ANGER_HIGH",
            name = "Wściekłość",
            emotionCategory = EmotionCategory.ANGER,
            intensity = EmotionIntensity.HIGH,
            description = "Intensywny gniew i furia.",
            examples = listOf("Wściekłość na niesprawiedliwość.", "Furia z powodu zdrady."),
            color = EmotionColors.ANGER.darker(0.2f)
        ),
        "ANGER_MEDIUM" to Emotion(
            id = "ANGER_MEDIUM",
            name = "Złość",
            emotionCategory = EmotionCategory.ANGER,
            intensity = EmotionIntensity.MEDIUM,
            description = "Uczucie gniewu i irytacji.",
            examples = listOf("Złość na korki.", "Gniew na niesprawiedliwość."),
            color = EmotionColors.ANGER
        ),
        "ANGER_LOW" to Emotion(
            id = "ANGER_LOW",
            name = "Irytacja",
            emotionCategory = EmotionCategory.ANGER,
            intensity = EmotionIntensity.LOW,
            description = "Łagodna irytacja i rozdrażnienie.",
            examples = listOf("Drobne niedogodności.", "Lekkie rozdrażnienie."),
            color = EmotionColors.ANGER.lighter(0.2f)
        ),

        // ANTICIPATION emotions
        "ANTICIPATION_HIGH" to Emotion(
            id = "ANTICIPATION_HIGH",
            name = "Czuwanie",
            emotionCategory = EmotionCategory.ANTICIPATION,
            intensity = EmotionIntensity.HIGH,
            description = "Intensywne oczekiwanie i czujność.",
            examples = listOf("Czuwanie nad chorym.", "Intensywne oczekiwanie."),
            color = EmotionColors.ANTICIPATION.darker(0.2f)
        ),
        "ANTICIPATION_MEDIUM" to Emotion(
            id = "ANTICIPATION_MEDIUM",
            name = "Oczekiwanie",
            emotionCategory = EmotionCategory.ANTICIPATION,
            intensity = EmotionIntensity.MEDIUM,
            description = "Uczucie oczekiwania i nadziei.",
            examples = listOf("Oczekiwanie na wyniki.", "Nadzieja na sukces."),
            color = EmotionColors.ANTICIPATION
        ),
        "ANTICIPATION_LOW" to Emotion(
            id = "ANTICIPATION_LOW",
            name = "Ciekawość",
            emotionCategory = EmotionCategory.ANTICIPATION,
            intensity = EmotionIntensity.LOW,
            description = "Łagodna ciekawość i zainteresowanie.",
            examples = listOf("Ciekawość nowości.", "Zainteresowanie tematem."),
            color = EmotionColors.ANTICIPATION.lighter(0.2f)
        )
    )

    // Complex Emotions (Diads)
    val complexEmotions = mapOf(
        "LOVE" to Emotion(
            id = "LOVE",
            name = "Miłość",
            primaryEmotion1 = EmotionCategory.JOY,
            primaryEmotion2 = EmotionCategory.TRUST,
            description = "Głębokie uczucie przywiązania i troski.",
            examples = listOf("Miłość do partnera.", "Miłość do dziecka."),
            color = Color(0xFFFF69B4) // Hot pink
        ),
        "SUBMISSION" to Emotion(
            id = "SUBMISSION",
            name = "Uległość",
            primaryEmotion1 = EmotionCategory.TRUST,
            primaryEmotion2 = EmotionCategory.FEAR,
            description = "Uległość i podporządkowanie się.",
            examples = listOf("Uległość wobec autorytetu.", "Podporządkowanie się regułom."),
            color = Color(0xFF808080) // Gray
        ),
        "AWE" to Emotion(
            id = "AWE",
            name = "Podziw",
            primaryEmotion1 = EmotionCategory.FEAR,
            primaryEmotion2 = EmotionCategory.SURPRISE,
            description = "Uczucie podziwu i szacunku.",
            examples = listOf("Podziw dla natury.", "Szacunek dla mistrza."),
            color = Color(0xFF4B0082) // Indigo
        ),
        "DISAPPROVAL" to Emotion(
            id = "DISAPPROVAL",
            name = "Niezgoda",
            primaryEmotion1 = EmotionCategory.SURPRISE,
            primaryEmotion2 = EmotionCategory.SADNESS,
            description = "Niezgoda i dezaprobata.",
            examples = listOf("Niezgoda z decyzją.", "Dezaprobata zachowania."),
            color = Color(0xFF8B4513) // Saddle brown
        ),
        "REMORSE" to Emotion(
            id = "REMORSE",
            name = "Skrucha",
            primaryEmotion1 = EmotionCategory.SADNESS,
            primaryEmotion2 = EmotionCategory.DISGUST,
            description = "Skrucha i żal za czyny.",
            examples = listOf("Żal za błędy.", "Skrucha za grzechy."),
            color = Color(0xFF2F4F4F) // Dark slate gray
        ),
        "CONTEMPT" to Emotion(
            id = "CONTEMPT",
            name = "Pogarda",
            primaryEmotion1 = EmotionCategory.DISGUST,
            primaryEmotion2 = EmotionCategory.ANGER,
            description = "Pogarda i lekceważenie.",
            examples = listOf("Pogarda dla wroga.", "Lekceważenie wartości."),
            color = Color(0xFF8B0000) // Dark red
        ),
        "AGGRESSIVENESS" to Emotion(
            id = "AGGRESSIVENESS",
            name = "Agresywność",
            primaryEmotion1 = EmotionCategory.ANGER,
            primaryEmotion2 = EmotionCategory.ANTICIPATION,
            description = "Agresywność i wrogość.",
            examples = listOf("Agresywność w sporcie.", "Wrogość wobec przeciwnika."),
            color = Color(0xFFFF4500) // Orange red
        ),
        "OPTIMISM" to Emotion(
            id = "OPTIMISM",
            name = "Optymizm",
            primaryEmotion1 = EmotionCategory.ANTICIPATION,
            primaryEmotion2 = EmotionCategory.JOY,
            description = "Optymizm i pozytywne nastawienie.",
            examples = listOf("Optymizm wobec przyszłości.", "Pozytywne nastawienie."),
            color = Color(0xFFFFD700) // Gold
        )
    )

    /**
     * Get a basic emotion by its type and intensity.
     */
    fun getEmotion(emotionCategory: EmotionCategory, intensity: EmotionIntensity): Emotion? {
        val key = "${emotionCategory.name}_${intensity.name}"
        return emotionsCategory[key]
    }

    /**
     * Get a complex emotion (diad) by its two primary emotions.
     */
    fun getCombinedEmotion(emotion1: EmotionCategory, emotion2: EmotionCategory): Emotion? {
        return complexEmotions.values.find { emotion ->
            (emotion.primaryEmotion1 == emotion1 && emotion.primaryEmotion2 == emotion2) ||
            (emotion.primaryEmotion1 == emotion2 && emotion.primaryEmotion2 == emotion1)
        }
    }

    /**
     * Get all basic emotions.
     */
    fun getAllBasicEmotions(): List<Emotion> = emotionsCategory.values.toList()

    /**
     * Get all complex emotions (diads).
     */
    fun getAllComplexEmotions(): List<Emotion> = complexEmotions.values.toList()

    /**
     * Get all emotions (basic + complex).
     */
    fun getAllEmotions(): List<Emotion> = emotionsCategory.values + complexEmotions.values
} 