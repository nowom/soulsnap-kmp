package pl.soulsnaps.di

import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import pl.soulsnaps.features.affirmation.AffirmationsViewModel
import pl.soulsnaps.features.capturemoment.CaptureMomentViewModel
import pl.soulsnaps.features.exersises.ExerciseRepository
import pl.soulsnaps.features.exersises.ExercisesViewModel
import pl.soulsnaps.features.exersises.GetCompletedExercisesUseCase
import pl.soulsnaps.features.exersises.InMemoryExerciseRepository
import pl.soulsnaps.features.exersises.MarkExerciseCompletedUseCase
import pl.soulsnaps.features.memoryhub.timeline.TimelineViewModel
import pl.soulsnaps.features.memoryhub.gallery.MomentsGalleryViewModel
import pl.soulsnaps.features.memoryhub.map.MemoryMapViewModel

object FeatureModule {
    fun get() = module {
        viewModelOf(::AffirmationsViewModel)
        viewModelOf(::CaptureMomentViewModel)
        viewModelOf(::MomentsGalleryViewModel)
        viewModelOf(::TimelineViewModel)
        viewModelOf(::MemoryMapViewModel)
        single<ExerciseRepository> { InMemoryExerciseRepository() }
        single { GetCompletedExercisesUseCase(get()) }
        single { MarkExerciseCompletedUseCase(get()) }

        viewModelOf(::ExercisesViewModel)
    }
}