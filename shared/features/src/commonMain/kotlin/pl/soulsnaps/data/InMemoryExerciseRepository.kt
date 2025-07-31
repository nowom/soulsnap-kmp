package pl.soulsnaps.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import pl.soulsnaps.domain.ExerciseRepository
import pl.soulsnaps.domain.model.EmotionCategory
import pl.soulsnaps.domain.model.Exercise

/**
 * Implementacja repozytorium ćwiczeń w pamięci (mock).
 * In-memory implementation of the exercise repository (mock).
 */
class InMemoryExerciseRepository : ExerciseRepository {

    private val _exercises = MutableStateFlow(
        listOf(
            Exercise(
                "ex1",
                "Oddech 4-7-8",
                "Technika oddechowa pomagająca w redukcji stresu i relaksacji.",
                EmotionCategory.ANTICIPATION
            ),
            Exercise(
                "ex2",
                "Dziennik Wdzięczności",
                "Zapisuj 3 rzeczy, za które jesteś wdzięczny każdego dnia.",
                EmotionCategory.JOY
            ),
            Exercise(
                "ex3",
                "Skan Ciała",
                "Ćwiczenie uważności, które pomaga zidentyfikować napięcia w ciele.",
                EmotionCategory.ANTICIPATION
            ),
            Exercise(
                "ex4",
                "Afirmacje Poranne",
                "Powtarzaj pozytywne stwierdzenia, aby wzmocnić poczucie własnej wartości.",
                EmotionCategory.JOY
            ),
            Exercise(
                "ex5",
                "Uwalnianie Złości",
                "Bezpieczne metody wyrażania i uwalniania nagromadzonej złości.",
                EmotionCategory.ANGER
            ),
            Exercise(
                "ex6",
                "Akceptacja Smutku",
                "Ćwiczenie pozwalające na przyjęcie i przetworzenie uczucia smutku.",
                EmotionCategory.SADNESS
            ),
        )
    )

    private val _completedExerciseIds = MutableStateFlow(emptySet<String>())

    override fun getAllExercises(): Flow<List<Exercise>> = _exercises

    override suspend fun markExerciseCompleted(exerciseId: String, completed: Boolean) {
        _exercises.update { currentExercises ->
            currentExercises.map { exercise ->
                if (exercise.id == exerciseId) {
                    exercise.copy(isCompleted = completed)
                } else {
                    exercise
                }
            }
        }
        _completedExerciseIds.update { currentSet ->
            if (completed) {
                currentSet + exerciseId
            } else {
                currentSet - exerciseId
            }
        }
    }

    override fun getCompletedExerciseIds(): Flow<Set<String>> = _completedExerciseIds
}