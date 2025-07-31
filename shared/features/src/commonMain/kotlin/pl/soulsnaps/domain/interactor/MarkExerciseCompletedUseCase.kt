package pl.soulsnaps.domain.interactor

import pl.soulsnaps.domain.ExerciseRepository

/**
 * Use case do oznaczania ćwiczenia jako ukończonego.
 * Use case for marking an exercise as completed.
 */
class MarkExerciseCompletedUseCase(private val repository: ExerciseRepository) {
    suspend operator fun invoke(exerciseId: String, completed: Boolean) {
        repository.markExerciseCompleted(exerciseId, completed)
    }
}