package pl.soulsnaps.domain.interactor

import kotlinx.coroutines.flow.Flow
import pl.soulsnaps.domain.ExerciseRepository

/**
 * Use case do pobierania ID ukończonych ćwiczeń.
 * Use case for getting IDs of completed exercises.
 */
class GetCompletedExercisesUseCase(private val repository: ExerciseRepository) {
    operator fun invoke(): Flow<Set<String>> {
        return repository.getCompletedExerciseIds()
    }
}