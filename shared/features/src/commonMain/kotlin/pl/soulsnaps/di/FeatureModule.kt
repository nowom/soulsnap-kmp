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
import pl.soulsnaps.features.onboarding.OnboardingDataStore
import pl.soulsnaps.features.analytics.AnalyticsManager
import pl.soulsnaps.features.analytics.AnalyticsRepository
import pl.soulsnaps.features.analytics.FakeAnalyticsRepository
import pl.soulsnaps.features.virtualmirror.VirtualMirrorViewModel
import pl.soulsnaps.features.auth.AuthViewModel

object FeatureModule {
    fun get() = module {
        viewModelOf(::AffirmationsViewModel)
        viewModelOf(::CaptureMomentViewModel)
        viewModelOf(::DashboardViewModel)
        viewModelOf(::MomentsGalleryViewModel)
        viewModelOf(::TimelineViewModel)
        viewModelOf(::MemoryMapViewModel)
        single<OnboardingDataStore> { 
            // For MVP, use in-memory implementation
            object : OnboardingDataStore {
                private var isCompleted = false
                override val onboardingCompleted: kotlinx.coroutines.flow.Flow<Boolean> = kotlinx.coroutines.flow.flowOf(isCompleted)
                override suspend fun clearOnboardingData() { isCompleted = false }
                override suspend fun markOnboardingCompleted() { isCompleted = true }
            }
        }
        viewModelOf(::OnboardingViewModel)
        viewModelOf(::VirtualMirrorViewModel)
        viewModelOf(::AuthViewModel)
        single<ExerciseRepository> { InMemoryExerciseRepository() }
        single { GetCompletedExercisesUseCase(get()) }
        single { MarkExerciseCompletedUseCase(get()) }

        // Analytics
        single<AnalyticsRepository> { FakeAnalyticsRepository() }
        single { 
            AnalyticsManager(
                repository = get(),
                coroutineScope = kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.SupervisorJob())
            )
        }

        viewModelOf(::ExercisesViewModel)
    }
}