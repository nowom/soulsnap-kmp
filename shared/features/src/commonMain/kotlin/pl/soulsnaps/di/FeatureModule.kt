package pl.soulsnaps.di

import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import pl.soulsnaps.features.affirmation.AffirmationsViewModel
import pl.soulsnaps.features.capturemoment.CaptureMomentViewModel
import pl.soulsnaps.features.dashboard.DashboardViewModel
import pl.soulsnaps.features.exersises.ExercisesViewModel
import pl.soulsnaps.domain.interactor.GetCompletedExercisesUseCase
import pl.soulsnaps.data.InMemoryExerciseRepository
import pl.soulsnaps.domain.ExerciseRepository
import pl.soulsnaps.domain.interactor.MarkExerciseCompletedUseCase
import pl.soulsnaps.features.memoryhub.timeline.TimelineViewModel
import pl.soulsnaps.features.memoryhub.gallery.MomentsGalleryViewModel
import pl.soulsnaps.features.memoryhub.map.MemoryMapViewModel
import pl.soulsnaps.features.onboarding.OnboardingViewModel
import pl.soulsnaps.features.virtualmirror.VirtualMirrorViewModel

object FeatureModule {
    fun get() = module {
        viewModelOf(::AffirmationsViewModel)
        viewModelOf(::CaptureMomentViewModel)
        viewModelOf(::DashboardViewModel)
        viewModelOf(::MomentsGalleryViewModel)
        viewModelOf(::TimelineViewModel)
        viewModelOf(::MemoryMapViewModel)
        viewModelOf(::OnboardingViewModel)
        viewModelOf(::VirtualMirrorViewModel)
        single<ExerciseRepository> { InMemoryExerciseRepository() }
        single { GetCompletedExercisesUseCase(get()) }
        single { MarkExerciseCompletedUseCase(get()) }

        viewModelOf(::ExercisesViewModel)
    }
}