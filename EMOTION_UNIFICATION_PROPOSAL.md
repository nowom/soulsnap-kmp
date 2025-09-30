# Ujednolicenie Systemu Emocji - Propozycja

## 🎯 Problem
Obecnie mamy 4 różne systemy emocji w aplikacji:
1. **MoodType** (domain) - 8 wartości
2. **EmotionType** (analysis) - 7 wartości  
3. **EmotionCategory** (Plutchik) - 8 wartości
4. **EmotionIntensity** - 3 poziomy

## 📋 Opcje Rozwiązania

### **Opcja 1: Uproszczenie do MoodType (REKOMENDOWANA)**
Używamy tylko `MoodType` z 8 wartościami, które już pasują do bazy danych:

```kotlin
enum class MoodType {
    HAPPY, SAD, EXCITED, CALM, ANXIOUS, GRATEFUL, LOVED, STRESSED
}
```

**Zalety:**
- ✅ Już pasuje do bazy danych
- ✅ Proste w użyciu
- ✅ Wystarczające dla większości przypadków
- ✅ Minimalne zmiany w kodzie

**Wady:**
- ❌ Mniej precyzyjne niż Plutchik
- ❌ Brak intensywności emocji

### **Opcja 2: Rozszerzenie MoodType o intensywność**
Dodajemy intensywność do istniejącego systemu:

```kotlin
data class Mood(
    val type: MoodType,
    val intensity: EmotionIntensity = EmotionIntensity.MEDIUM
)

enum class MoodType {
    HAPPY, SAD, EXCITED, CALM, ANXIOUS, GRATEFUL, LOVED, STRESSED
}

enum class EmotionIntensity {
    LOW, MEDIUM, HIGH
}
```

**Zalety:**
- ✅ Zachowuje kompatybilność z bazą danych
- ✅ Dodaje precyzję
- ✅ Elastyczne

**Wady:**
- ❌ Więcej zmian w kodzie
- ❌ Trudniejsze w użyciu

### **Opcja 3: Pełny system Plutchik**
Używamy kompletnego systemu Plutchik z 8 podstawowymi emocjami:

```kotlin
enum class EmotionCategory {
    JOY, TRUST, FEAR, SURPRISE, SADNESS, DISGUST, ANGER, ANTICIPATION
}

enum class EmotionIntensity {
    LOW, MEDIUM, HIGH
}

data class Emotion(
    val category: EmotionCategory,
    val intensity: EmotionIntensity
)
```

**Zalety:**
- ✅ Naukowo uzasadnione
- ✅ Bardzo precyzyjne
- ✅ Profesjonalne

**Wady:**
- ❌ Wymaga zmiany bazy danych
- ❌ Bardzo dużo zmian w kodzie
- ❌ Skomplikowane dla użytkowników

## 🎯 REKOMENDACJA: Opcja 1

**Dlaczego Opcja 1:**
1. **Minimalne zmiany** - już mamy działający system
2. **Kompatybilność** - pasuje do bazy danych
3. **Prostota** - łatwe w użyciu dla użytkowników
4. **Wystarczające** - 8 emocji pokrywa większość przypadków

## 🔧 Plan Implementacji (Opcja 1)

### Krok 1: Usunięcie niepotrzebnych enumów
- Usuń `EmotionType` z analizy twarzy
- Usuń `EmotionCategory` (Plutchik)
- Zostaw tylko `MoodType`

### Krok 2: Mapowanie EmotionType → MoodType
```kotlin
fun EmotionType.toMoodType(): MoodType {
    return when (this) {
        EmotionType.HAPPY -> MoodType.HAPPY
        EmotionType.SAD -> MoodType.SAD
        EmotionType.ANGRY -> MoodType.STRESSED
        EmotionType.SURPRISED -> MoodType.EXCITED
        EmotionType.FEARFUL -> MoodType.ANXIOUS
        EmotionType.DISGUSTED -> MoodType.SAD
        EmotionType.NEUTRAL -> MoodType.CALM
    }
}
```

### Krok 3: Aktualizacja bazy danych
Sprawdź czy CHECK constraint jest potrzebny:

```sql
-- Obecne
mood TEXT CHECK(mood IN ('happy', 'sad', 'excited', 'calm', 'anxious', 'grateful', 'loved', 'stressed'))

-- Można uprościć do:
mood TEXT CHECK(mood IN ('happy', 'sad', 'excited', 'calm', 'anxious', 'grateful', 'loved', 'stressed'))
-- Lub nawet usunąć CHECK constraint jeśli ufamy aplikacji
```

## ❓ Pytania do Rozważenia

1. **Czy CHECK constraint jest potrzebny?**
   - ✅ **TAK** - zabezpiecza przed błędami
   - ❌ **NIE** - aplikacja kontroluje wartości

2. **Czy 8 emocji wystarczy?**
   - ✅ **TAK** - pokrywa większość przypadków
   - ❌ **NIE** - potrzebujemy więcej precyzji

3. **Czy intensywność emocji jest ważna?**
   - ✅ **TAK** - dodaje precyzję
   - ❌ **NIE** - komplikuje system

## 🎯 Finalna Rekomendacja

**Użyj Opcji 1** - uproszczenie do MoodType z 8 wartościami:

1. **Zachowaj** `MoodType` jako główny enum
2. **Usuń** `EmotionType`, `EmotionCategory`
3. **Zachowaj** CHECK constraint w bazie danych
4. **Dodaj** mapowanie z analizy twarzy do MoodType
5. **Uprość** kod usuwając niepotrzebne enumy

To da nam:
- ✅ Spójny system emocji
- ✅ Minimalne zmiany w kodzie  
- ✅ Kompatybilność z bazą danych
- ✅ Łatwość w utrzymaniu
