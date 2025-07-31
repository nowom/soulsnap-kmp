//// commonMain/kotlin/com/your_app_name/emotionsapp/models/Emotion.kt
//
//package com.your_app_name.emotionsapp.models
//
//import androidx.compose.ui.graphics.Color
//import kotlin.math.PI
//import kotlin.math.max
//import kotlin.math.min
//
//// --- Enums dla Emocji ---
//enum class BasicEmotion {
//    JOY, // Radość
//    TRUST, // Zaufanie
//    FEAR, // Strach
//    SURPRISE, // Zaskoczenie
//    SADNESS, // Smutek
//    DISGUST, // Wstręt
//    ANGER, // Złość
//    ANTICIPATION // Antycypacja
//}
//
//enum class EmotionIntensity {
//    LOW,
//    MEDIUM,
//    HIGH
//}
//
///**
// * Definicja pojedynczej emocji w Kole Plutchika.
// * Może to być emocja podstawowa na danym poziomie intensywności lub emocja złożona.
// */
//data class Emotion(
//    val id: String, // Unikalny identyfikator emocji (np. "JOY_MEDIUM", "LOVE")
//    val name: String, // Nazwa emocji w języku polskim
//    val basicEmotion: BasicEmotion? = null, // Emocja podstawowa, jeśli to emocja podstawowa
//    val intensity: EmotionIntensity? = null, // Poziom intensywności, jeśli to emocja podstawowa
//    val primaryEmotion1: BasicEmotion? = null, // Pierwsza emocja podstawowa dla diady/triady
//    val primaryEmotion2: BasicEmotion? = null, // Druga emocja podstawowa dla diady/triady
//    // val primaryEmotion3: BasicEmotion? = null, // Można dodać dla triad, jeśli będziesz je implementować
//    val description: String, // Krótki opis emocji
//    val examples: List<String>, // Przykładowe sytuacje, w których można odczuwać tę emocję
//    val color: Color // Kolor reprezentujący emocję na kole
//)
//
//// --- Definicje Kolorów Bazowych dla 8 Emocji Podstawowych (wg. Plutchika) ---
//object EmotionColors {
//    // Kolory Plutchika
//    val JOY = Color(0xFFFFF59D) // Jasny żółty (Sunflower)
//    val TRUST = Color(0xFFA5D6A7) // Pastelowa zieleń (Mint)
//    val FEAR = Color(0xFF80CBC4) // Turkus miętowy (Calm Aqua)
//    val SURPRISE = Color(0xFF81D4FA) // Jasny błękit (Sky Blue)
//    val SADNESS = Color(0xFF90CAF9) // Pastelowy niebieski (Cornflower)
//    val DISGUST = Color(0xFFCE93D8) // Jasny fiolet/róż (Lilac)
//    val ANGER = Color(0xFFEF9A9A) // Jasna czerwień (Watermelon red)
//    val ANTICIPATION = Color(0xFFFFCC80) // Jasny pomarańcz (Peach)
//
//    // Dopasowanie do obrazka, jeśli oryginalne kolory Plutchika są zbyt intensywne:
////     val JOY = Color(0xFFFFF700) // Lżejszy żółty
////     val TRUST = Color(0xFF00E000) // Lżejszy zielony
////     val FEAR = Color(0xFF9000FF) // Bardziej żywy fiolet
////     val SURPRISE = Color(0xFF00C0FF) // Bardziej niebieski cyjan
////     val SADNESS = Color(0xFF0000E0) // Nieco ciemniejszy niebieski
////     val DISGUST = Color(0xFFBB00BB) // Bardziej różowy fiolet
////     val ANGER = Color(0xFFFF4040) // Jaśniejsza czerwień
////     val ANTICIPATION = Color(0xFFFFC000) // Jaśniejszy pomarańczowy
//}
//
//// --- Funkcje pomocnicze do manipulacji kolorami ---
///**
// * Zwraca jaśniejszy odcień koloru.
// * @param factor Współczynnik rozjaśnienia (0.0f - brak zmiany, 1.0f - do bieli).
// */
//fun Color.lighter(factor: Float = 0.2f): Color { // Zwiększyłem domyślny factor dla bardziej widocznej zmiany
//    val red = (this.red + (1f - this.red) * factor).coerceIn(0f, 1f)
//    val green = (this.green + (1f - this.green) * factor).coerceIn(0f, 1f)
//    val blue = (this.blue + (1f - this.blue) * factor).coerceIn(0f, 1f)
//    return Color(red, green, blue, this.alpha)
//}
//
///**
// * Zwraca ciemniejszy odcień koloru.
// * @param factor Współczynnik przyciemnienia (0.0f - brak zmiany, 1.0f - do czerni).
// */
//fun Color.darker(factor: Float = 0.2f): Color { // Zwiększyłem domyślny factor
//    val red = (this.red * (1f - factor)).coerceIn(0f, 1f)
//    val green = (this.green * (1f - factor)).coerceIn(0f, 1f)
//    val blue = (this.blue * (1f - factor)).coerceIn(0f, 1f)
//    return Color(red, green, blue, this.alpha)
//}
//
///**
// * Liniowa interpolacja między dwoma kolorami.
// * @param start Kolor początkowy.
// * @param end Kolor końcowy.
// * @param fraction Proporcja (0.0f - start, 1.0f - end).
// */
//fun Color.Companion.Lerp(start: Color, end: Color, fraction: Float): Color {
//    return Color(
//        red = lerp(start.red, end.red, fraction),
//        green = lerp(start.green, end.green, fraction),
//        blue = lerp(start.blue, end.blue, fraction),
//        alpha = lerp(start.alpha, end.alpha, fraction)
//    )
//}
//
///**
// * Liniowa interpolacja dla wartości zmiennoprzecinkowych.
// */
//fun lerp(start: Float, stop: Float, fraction: Float): Float {
//    return start + (stop - start) * fraction
//}
//
//
//// --- Obiekt zawierający wszystkie zdefiniowane emocje Plutchika ---
//object EmotionData {
//    val allEmotions: List<Emotion> by lazy {
//        listOf(
//            // --- Emocje podstawowe i ich intensywności (Polskie nazwy z obrazka) ---
//            // JOY (Radość)
//            Emotion(
//                id = "JOY_LOW", name = "Błogość", basicEmotion = BasicEmotion.JOY, intensity = EmotionIntensity.LOW,
//                description = "Niski poziom radości, uczucie spokoju i zadowolenia.",
//                examples = listOf("Poranna kawa na balkonie.", "Czytanie ulubionej książki."),
//                color = EmotionColors.JOY.lighter(0.3f)// Lekko jaśniejsza radość
//            ),
//            Emotion(
//                id = "JOY_MEDIUM", name = "Radość", basicEmotion = BasicEmotion.JOY, intensity = EmotionIntensity.MEDIUM,
//                description = "Podstawowe uczucie szczęścia i zadowolenia.",
//                examples = listOf("Spotkanie ze starym przyjacielem.", "Otrzymanie dobrej wiadomości."),
//                color = EmotionColors.JOY
//            ),
//            Emotion(
//                id = "JOY_HIGH", name = "Ekstaza", basicEmotion = BasicEmotion.JOY, intensity = EmotionIntensity.HIGH,
//                description = "Bardzo intensywne uczucie radości, euforii i szczęścia.",
//                examples = listOf("Zwycięstwo w ważnym konkursie.", "Urodzenie dziecka."),
//                color = EmotionColors.JOY.darker(0.2f) // Lekko ciemniejsza radość
//            ),
//
//            // TRUST (Zaufanie)
//            Emotion(
//                id = "TRUST_LOW", name = "Akceptacja", basicEmotion = BasicEmotion.TRUST, intensity = EmotionIntensity.LOW,
//                description = "Niski poziom zaufania, uczucie akceptacji i otwartości.",
//                examples = listOf("Zgoda na propozycję.", "Przyjmowanie faktów."),
//                color = EmotionColors.TRUST.lighter(0.3f)
//            ),
//            Emotion(
//                id = "TRUST_MEDIUM", name = "Zaufanie", basicEmotion = BasicEmotion.TRUST, intensity = EmotionIntensity.MEDIUM,
//                description = "Podstawowe uczucie wiary w kogoś lub coś.",
//                examples = listOf("Zaufanie do przyjaciela.", "Wierzyć w obietnice."),
//                color = EmotionColors.TRUST
//            ),
//            Emotion(
//                id = "TRUST_HIGH", name = "Podziw", basicEmotion = BasicEmotion.TRUST, intensity = EmotionIntensity.HIGH,
//                description = "Bardzo intensywne uczucie zaufania połączone z szacunkiem.",
//                examples = listOf("Podziw dla bohatera.", "Cześć dla mędrca."),
//                color = EmotionColors.TRUST.darker(0.2f)
//            ),
//
//            // FEAR (Strach)
//            Emotion(
//                id = "FEAR_LOW", name = "Lęk", basicEmotion = BasicEmotion.FEAR, intensity = EmotionIntensity.LOW,
//                description = "Niski poziom strachu, lekkie zaniepokojenie.",
//                examples = listOf("Obawa przed spóźnieniem.", "Niepokój o przyszłość."),
//                color = EmotionColors.FEAR.lighter(0.3f)
//            ),
//            Emotion(
//                id = "FEAR_MEDIUM", name = "Strach", basicEmotion = BasicEmotion.FEAR, intensity = EmotionIntensity.MEDIUM,
//                description = "Podstawowe uczucie zagrożenia.",
//                examples = listOf("Strach przed ciemnością.", "Lęk wysokości."),
//                color = EmotionColors.FEAR
//            ),
//            Emotion(
//                id = "FEAR_HIGH", name = "Przerażenie", basicEmotion = BasicEmotion.FEAR, intensity = EmotionIntensity.HIGH,
//                description = "Bardzo intensywne uczucie strachu, paniki.",
//                examples = listOf("Terror w obliczu katastrofy.", "Paraliżujący lęk."),
//                color = EmotionColors.FEAR.darker(0.2f)
//            ),
//
//            // SURPRISE (Zaskoczenie)
//            Emotion(
//                id = "SURPRISE_LOW", name = "Roztargnienie", basicEmotion = BasicEmotion.SURPRISE, intensity = EmotionIntensity.LOW,
//                description = "Niski poziom zaskoczenia, lekka dezorientacja.",
//                examples = listOf("Lekkie zdziwienie.", "Chwilowe rozkojarzenie."),
//                color = EmotionColors.SURPRISE.lighter(0.3f)
//            ),
//            Emotion(
//                id = "SURPRISE_MEDIUM", name = "Zaskoczenie", basicEmotion = BasicEmotion.SURPRISE, intensity = EmotionIntensity.MEDIUM,
//                description = "Podstawowe uczucie niespodzianki.",
//                examples = listOf("Niespodziewany prezent.", "Nagłe pojawienie się kogoś."),
//                color = EmotionColors.SURPRISE
//            ),
//            Emotion(
//                id = "SURPRISE_HIGH", name = "Zdumienie", basicEmotion = BasicEmotion.SURPRISE, intensity = EmotionIntensity.HIGH,
//                description = "Bardzo intensywne uczucie zaskoczenia, szoku.",
//                examples = listOf("Zdumienie odkryciem.", "Oszołomienie."),
//                color = EmotionColors.SURPRISE.darker(0.2f)
//            ),
//
//            // SADNESS (Smutek)
//            Emotion(
//                id = "SADNESS_LOW", name = "Melancholia", basicEmotion = BasicEmotion.SADNESS, intensity = EmotionIntensity.LOW,
//                description = "Niski poziom smutku, refleksja, melancholia.",
//                examples = listOf("Wspominanie przeszłości.", "Zamyślenie."),
//                color = EmotionColors.SADNESS.lighter(0.3f)
//            ),
//            Emotion(
//                id = "SADNESS_MEDIUM", name = "Smutek", basicEmotion = BasicEmotion.SADNESS, intensity = EmotionIntensity.MEDIUM,
//                description = "Podstawowe uczucie przygnębienia, żalu.",
//                examples = listOf("Strata bliskiej osoby.", "Niepowodzenie."),
//                color = EmotionColors.SADNESS
//            ),
//            Emotion(
//                id = "SADNESS_HIGH", name = "Żal", basicEmotion = BasicEmotion.SADNESS, intensity = EmotionIntensity.HIGH,
//                description = "Bardzo intensywne uczucie smutku, rozpaczy.",
//                examples = listOf("Głęboki żal po stracie.", "Stan depresyjny."),
//                color = EmotionColors.SADNESS.darker(0.2f)
//            ),
//
//            // DISGUST (Wstręt)
//            Emotion(
//                id = "DISGUST_LOW", name = "Nuda", basicEmotion = BasicEmotion.DISGUST, intensity = EmotionIntensity.LOW,
//                description = "Niski poziom wstrętu, uczucie znudzenia lub lekkiej niechęci.",
//                examples = listOf("Monotonna praca.", "Niewyobrażalnie nudny wykład."),
//                color = EmotionColors.DISGUST.lighter(0.3f)
//            ),
//            Emotion(
//                id = "DISGUST_MEDIUM", name = "Niesmak", basicEmotion = BasicEmotion.DISGUST, intensity = EmotionIntensity.MEDIUM,
//                description = "Podstawowe uczucie odrzucenia, obrzydzenia.",
//                examples = listOf("Wstręt do zepsutego jedzenia.", "Odrzucenie niesprawiedliwego zachowania."),
//                color = EmotionColors.DISGUST
//            ),
//            Emotion(
//                id = "DISGUST_HIGH", name = "Wstręt", basicEmotion = BasicEmotion.DISGUST, intensity = EmotionIntensity.HIGH, // Nazwa "Wstręt" na obrazie jest na wysokiej intensywności
//                description = "Bardzo intensywne uczucie wstrętu, silna repulsja.",
//                examples = listOf("Obrzydzenie do okrucieństwa.", "Silna reakcja na nieprzyjemny zapach."),
//                color = EmotionColors.DISGUST.darker(0.2f)
//            ),
//
//            // ANGER (Złość)
//            Emotion(
//                id = "ANGER_LOW", name = "Irytacja", basicEmotion = BasicEmotion.ANGER, intensity = EmotionIntensity.LOW,
//                description = "Niski poziom złości, drobne zdenerwowanie.",
//                examples = listOf("Stanie w korku.", "Drobne nieporozumienie."),
//                color = EmotionColors.ANGER.lighter(0.3f)
//            ),
//            Emotion(
//                id = "ANGER_MEDIUM", name = "Złość", basicEmotion = BasicEmotion.ANGER, intensity = EmotionIntensity.MEDIUM,
//                description = "Podstawowe uczucie gniewu, frustracji.",
//                examples = listOf("Reakcja na niesprawiedliwość.", "Gniew na krzywdę."),
//                color = EmotionColors.ANGER
//            ),
//            Emotion(
//                id = "ANGER_HIGH", name = "Wściekłość", basicEmotion = BasicEmotion.ANGER, intensity = EmotionIntensity.HIGH,
//                description = "Bardzo intensywne uczucie złości, furia.",
//                examples = listOf("Wybuch gniewu.", "Niepohamowana furia."),
//                color = EmotionColors.ANGER.darker(0.2f)
//            ),
//
//            // ANTICIPATION (Antycypacja)
//            Emotion(
//                id = "ANTICIPATION_LOW", name = "Ciekawość", basicEmotion = BasicEmotion.ANTICIPATION, intensity = EmotionIntensity.LOW,
//                description = "Niski poziom antycypacji, ciekawość, zainteresowanie.",
//                examples = listOf("Ciekawość nowej książki.", "Zainteresowanie czyjąś historią."),
//                color = EmotionColors.ANTICIPATION.lighter(0.3f)
//            ),
//            Emotion(
//                id = "ANTICIPATION_MEDIUM", name = "Oczekiwanie", basicEmotion = BasicEmotion.ANTICIPATION, intensity = EmotionIntensity.MEDIUM,
//                description = "Podstawowe uczucie oczekiwania, nadziei.",
//                examples = listOf("Oczekiwanie na podróż.", "Nadzieja na spotkanie."),
//                color = EmotionColors.ANTICIPATION
//            ),
//            Emotion(
//                id = "ANTICIPATION_HIGH", name = "Czuwanie", basicEmotion = BasicEmotion.ANTICIPATION, intensity = EmotionIntensity.HIGH,
//                description = "Bardzo intensywne uczucie antycypacji, czujność, wgotowość.",
//                examples = listOf("Czuwanie przed egzaminem.", "Stan gotowości bojowej."),
//                color = EmotionColors.ANTICIPATION.darker(0.2f)
//            ),
//
//            // --- Emocje złożone (Diady Pierwotne - nazwy z obrazka) ---
//            Emotion(
//                id = "OPTIMISM", name = "Optymizm", primaryEmotion1 = BasicEmotion.ANTICIPATION, primaryEmotion2 = BasicEmotion.JOY,
//                description = "Połączenie antycypacji i radości, pozytywne nastawienie do przyszłości.",
//                examples = listOf("Nadzieja na lepsze jutro.", "Oczekiwanie na dobre wydarzenia."),
//                color = Color.Lerp(EmotionColors.ANTICIPATION, EmotionColors.JOY, 0.5f)
//            ),
//            Emotion(
//                id = "LOVE", name = "Miłość", primaryEmotion1 = BasicEmotion.JOY, primaryEmotion2 = BasicEmotion.TRUST,
//                description = "Połączenie radości i zaufania, uczucie głębokiego przywiązania.",
//                examples = listOf("Miłość do partnera/partnerki.", "Miłość rodzicielska."),
//                color = Color.Lerp(EmotionColors.JOY, EmotionColors.TRUST, 0.5f)
//            ),
//            Emotion(
//                id = "SUBMISSION", name = "Poddanie", primaryEmotion1 = BasicEmotion.TRUST, primaryEmotion2 = BasicEmotion.FEAR,
//                description = "Połączenie zaufania i strachu, tendencja do poddania się.",
//                examples = listOf("Podporządkowanie się autorytetowi.", "Uległość wobec silniejszego."),
//                color = Color.Lerp(EmotionColors.TRUST, EmotionColors.FEAR, 0.5f)
//            ),
//            Emotion(
//                id = "AWE", name = "Groza", primaryEmotion1 = BasicEmotion.FEAR, primaryEmotion2 = BasicEmotion.SURPRISE,
//                description = "Połączenie strachu i zaskoczenia, uczucie podziwu połączonego z przerażeniem.",
//                examples = listOf("Reakcja na potężne zjawisko naturalne.", "Spotkanie z czymś nieznanym i potężnym."),
//                color = Color.Lerp(EmotionColors.FEAR, EmotionColors.SURPRISE, 0.5f)
//            ),
//            Emotion(
//                id = "DISAPPROVAL", name = "Dezaprobata", primaryEmotion1 = BasicEmotion.SURPRISE, primaryEmotion2 = BasicEmotion.SADNESS,
//                description = "Połączenie zaskoczenia i smutku, uczucie niezrealizowanych oczekiwań.",
//                examples = listOf("Niewygraną w loterii.", "Nieudany plan."),
//                color = Color.Lerp(EmotionColors.SURPRISE, EmotionColors.SADNESS, 0.5f)
//            ),
//            Emotion(
//                id = "REMORSE", name = "Wyrzuty Sumienia", primaryEmotion1 = BasicEmotion.SADNESS, primaryEmotion2 = BasicEmotion.DISGUST,
//                description = "Połączenie smutku i wstrętu, uczucie żalu i obrzydzenia do siebie.",
//                examples = listOf("Żal za popełnione błędy.", "Wstyd za swoje zachowanie."),
//                color = Color.Lerp(EmotionColors.SADNESS, EmotionColors.DISGUST, 0.5f)
//            ),
//            Emotion(
//                id = "CONTEMPT", name = "Pogarda", primaryEmotion1 = BasicEmotion.DISGUST, primaryEmotion2 = BasicEmotion.ANGER,
//                description = "Połączenie wstrętu i złości, uczucie wyższości i lekceważenia.",
//                examples = listOf("Pogarda dla oszusta.", "Lekceważenie czyjejś opinii."),
//                color = Color.Lerp(EmotionColors.DISGUST, EmotionColors.ANGER, 0.5f)
//            ),
//            Emotion(
//                id = "AGGRESSION", name = "Agresywność", primaryEmotion1 = BasicEmotion.ANGER, primaryEmotion2 = BasicEmotion.ANTICIPATION,
//                description = "Połączenie złości i antycypacji, gotowość do ataku lub konfrontacji.",
//                examples = listOf("Gotowość do walki.", "Wykonywanie czynności z agresywnym nastawieniem."),
//                color = Color.Lerp(EmotionColors.ANGER, EmotionColors.ANTICIPATION, 0.5f)
//            )
//        )
//    }
//
//    /**
//     * Zwraca emocję podstawową o podanej intensywności.
//     */
//    fun getEmotion(basicEmotion: BasicEmotion, intensity: EmotionIntensity): Emotion? {
//        return allEmotions.find {
//            it.basicEmotion == basicEmotion && it.intensity == intensity
//        }
//    }
//
//    /**
//     * Zwraca emocję złożoną (diadę pierwotną) na podstawie dwóch emocji podstawowych.
//     * Kolejność emocji nie ma znaczenia.
//     */
//    fun getCombinedEmotion(em1: BasicEmotion, em2: BasicEmotion): Emotion? {
//        return allEmotions.find { emotion ->
//            emotion.primaryEmotion1 != null && emotion.primaryEmotion2 != null &&
//                    ((emotion.primaryEmotion1 == em1 && emotion.primaryEmotion2 == em2) ||
//                            (emotion.primaryEmotion1 == em2 && emotion.primaryEmotion2 == em1))
//        }
//    }
//}