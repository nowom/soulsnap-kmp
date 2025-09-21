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
import pl.soulsnaps.features.auth.LoginViewModel
import pl.soulsnaps.features.auth.UserSessionManager
import pl.soulsnaps.features.memoryhub.details.MemoryDetailsViewModel
import pl.soulsnaps.features.memoryhub.edit.EditMemoryViewModel
import pl.soulsnaps.features.settings.SettingsViewModel
import pl.soulsnaps.features.location.LocationSearchService
import pl.soulsnaps.features.location.MapboxLocationSearchService
import pl.soulsnaps.features.location.LocationPickerViewModel
import pl.soulsnaps.features.location.LocationService
import pl.soulsnaps.features.location.LocationServiceFactory
import pl.soulsnaps.features.location.LocationPermissionManager
import pl.soulsnaps.features.location.LocationPermissionManagerFactory
import pl.soulsnaps.data.network.SoulSnapApi
import pl.soulsnaps.access.manager.UserPlanManager
import pl.soulsnaps.access.manager.AppStartupManager
import pl.soulsnaps.access.manager.OnboardingManager
import pl.soulsnaps.access.manager.PlanRegistryReader
import pl.soulsnaps.access.manager.PlanRegistryReaderImpl
import pl.soulsnaps.access.storage.UserPreferencesStorage
import pl.soulsnaps.access.storage.UserPreferencesStorageFactory
import pl.soulsnaps.access.guard.GuardFactory
import pl.soulsnaps.network.SupabaseAuthService

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
        viewModelOf(::LoginViewModel)
        viewModelOf(::MemoryDetailsViewModel)
        viewModelOf(::EditMemoryViewModel)
        viewModelOf(::SettingsViewModel)
        viewModelOf(::LocationPickerViewModel)

        // UserPreferencesStorage - singleton for user preferences
        single { UserPreferencesStorageFactory.create() }
        
        // UserPlanManager - singleton for managing user plans
        single { UserPlanManager(get()) }
        
        // UserPlanManagerInterface - alias for UserPlanManager (for dependency injection)
        single<pl.soulsnaps.access.guard.UserPlanManagerInterface> { get<UserPlanManager>() }
        
        // OnboardingManager - singleton for managing onboarding
        single { OnboardingManager(get()) }
        
        // PlanRegistryReader - singleton for plan registry
        single<PlanRegistryReader> { PlanRegistryReaderImpl() }
        
        // AccessGuard - singleton for access control
        single { GuardFactory.createDefaultGuard(get()) }
        
        // AppStartupManager - singleton for managing app startup (now with auth service)
        single { AppStartupManager(get(), get(), get<SupabaseAuthService>()) }
        
        // SoulSnapApi - singleton for centralized API client
        single { SoulSnapApi() }
        
        // LocationPermissionManager - singleton for permission handling
        single<LocationPermissionManager> { LocationPermissionManagerFactory.create() }
        
        // LocationService - singleton for GPS location
        single<LocationService> { LocationServiceFactory.create(get()) }
        
        // LocationSearchService - singleton for location autocomplete
        single<LocationSearchService> { MapboxLocationSearchService(get()) }

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