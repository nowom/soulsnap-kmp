package pl.soulsnaps.domain

import kotlinx.coroutines.flow.Flow
import pl.soulsnaps.domain.model.Exercise

interface ExerciseRepository {
    fun getAllExercises(): Flow<List<Exercise>>
    suspend fun markExerciseCompleted(exerciseId: String, completed: Boolean)
    fun getCompletedExerciseIds(): Flow<Set<String>>
}
