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
import pl.soulsnaps.domain.MemoryRepository
import pl.soulsnaps.features.memoryhub.timeline.TimelineViewModel
import pl.soulsnaps.features.memoryhub.gallery.MomentsGalleryViewModel
import pl.soulsnaps.features.memoryhub.map.MemoryMapViewModel
import pl.soulsnaps.features.onboarding.OnboardingViewModel
import pl.soulsnaps.features.analytics.AnalyticsManager
import pl.soulsnaps.features.analytics.AnalyticsRepository
import pl.soulsnaps.features.analytics.FakeAnalyticsRepository
import pl.soulsnaps.features.startup.SplashViewModel
import pl.soulsnaps.features.virtualmirror.VirtualMirrorViewModel
import pl.soulsnaps.features.auth.AuthViewModel
import pl.soulsnaps.features.auth.LoginViewModel
import pl.soulsnaps.features.auth.RegistrationViewModel
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
import pl.soulsnaps.features.coach.dailyquiz.DailyQuizViewModel
import pl.soulsnaps.features.settings.NotificationSettingsViewModel
import pl.soulsnaps.features.notifications.NotificationPermissionDialogViewModel
import pl.soulsnaps.features.upgrade.UpgradeViewModel
import pl.soulsnaps.features.upgrade.UpgradeRecommendationEngine
import pl.soulsnaps.domain.repository.EmotionQuizRepository
import pl.soulsnaps.data.EmotionQuizRepositoryImpl
import pl.soulsnaps.domain.interactor.*
import pl.soulsnaps.data.MockEmotionAIService
import pl.soulsnaps.features.notifications.NotificationService
import pl.soulsnaps.features.notifications.NotificationServiceFactory
import pl.soulsnaps.features.notifications.ReminderManager
import pl.soulsnaps.features.notifications.NotificationPermissionManager
import pl.soulsnaps.features.notifications.NotificationPermissionManagerFactory
import pl.soulsnaps.domain.repository.NotificationRepository
import pl.soulsnaps.data.NotificationRepositoryImpl
import pl.soulsnaps.access.manager.UserPlanManager
import pl.soulsnaps.access.manager.OnboardingManager
import pl.soulsnaps.access.manager.PlanRegistryReader
import pl.soulsnaps.access.manager.PlanRegistryReaderImpl
import pl.soulsnaps.access.storage.UserPreferencesStorage
import pl.soulsnaps.access.guard.GuardFactory
import pl.soulsnaps.access.manager.UserPlanManagerImpl
import pl.soulsnaps.access.storage.UserPreferencesStorageImpl
import pl.soulsnaps.features.memoryanalysis.service.MemoryAnalysisService
import pl.soulsnaps.features.memoryanalysis.engine.PatternDetectionEngine
import pl.soulsnaps.features.auth.UserSessionManager
import pl.soulsnaps.features.auth.UserSessionManagerImpl
import pl.soulsnaps.features.auth.GuestToUserMigration
import pl.soulsnaps.network.SupabaseAuthService
import pl.soulsnaps.storage.LocalStorageManager

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
        viewModelOf(::AuthViewModel)
        viewModelOf(::LoginViewModel)
        viewModelOf(::RegistrationViewModel)
        viewModelOf(::MemoryDetailsViewModel)
        viewModelOf(::EditMemoryViewModel)
        viewModelOf(::SettingsViewModel)
        viewModelOf(::LocationPickerViewModel)
        viewModelOf(::UpgradeViewModel)
        viewModelOf(::ExercisesViewModel)
        viewModelOf(::SplashViewModel)

        // UserPreferencesStorage - singleton for user preferences
        single<UserPreferencesStorage> { UserPreferencesStorageImpl(get()) }

        // UserPlanManager - singleton for managing user plans
        single<UserPlanManager> {
            UserPlanManagerImpl(
                storage = get(),
                userPlanRepository = get(),
                userPlanUseCase = get(),
                crashlyticsManager = get(),
                userSessionManager = get()
            )
        }

        // OnboardingManager - singleton for managing onboarding
        single { OnboardingManager(get()) }

        // PlanRegistryReader - singleton for plan registry
        single<PlanRegistryReader> {
            PlanRegistryReaderImpl(
                userPlanRepository = get(),
                crashlyticsManager = get()
            )
        }


        // SoulSnapApi - singleton for centralized API client
        single { SoulSnapApi() }

        // LocationPermissionManager - singleton for permission handling
        single<LocationPermissionManager> { LocationPermissionManagerFactory.create() }

        // LocationService - singleton for GPS location
        single<LocationService> { LocationServiceFactory.create(get()) }

        // LocationSearchService - singleton for location autocomplete
        single<LocationSearchService> { MapboxLocationSearchService(get()) }

        // Emotion Quiz - Daily quiz functionality
        single<EmotionQuizRepository> { EmotionQuizRepositoryImpl() }
        single<EmotionAIService> { MockEmotionAIService() }

        // Notifications - Notification and reminder system
        single<NotificationPermissionManager> { NotificationPermissionManagerFactory.create() }
        single<NotificationService> { NotificationServiceFactory.create() }
        single<NotificationRepository> { NotificationRepositoryImpl() }
        single { ReminderManager(get(), get()) }

        // Daily Quiz ViewModels
        viewModelOf(::DailyQuizViewModel)

        // Settings ViewModels
        viewModelOf(::NotificationSettingsViewModel)

        // Notification Permission Dialog ViewModel
        viewModelOf(::NotificationPermissionDialogViewModel)

        // Upgrade Recommendation Engine
        single { UpgradeRecommendationEngine(get()) }

        single<ExerciseRepository> { InMemoryExerciseRepository() }
        single { GetCompletedExercisesUseCase(get()) }
        single { MarkExerciseCompletedUseCase(get()) }

        // Analytics
        single<AnalyticsRepository> { FakeAnalyticsRepository() }
        single {
            AnalyticsManager(
                repository = get(),
                crashlyticsManager = get(),
                firebaseAnalytics = get(),
                coroutineScope = kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.SupervisorJob())
            )
        }
        
        // Guest to User Migration
        single {
            GuestToUserMigration(
                memoryDao = get(),
                onlineDataSource = get(),
                userSessionManager = get(),
                userPlanManager = get(),
                crashlyticsManager = get(),
                offlineSyncQueue = get(),
                offlineSyncProcessor = get()
            )
        }

        // Image Analyzer and Pattern Detection Engine
        //single<ImageAnalyzerInterface> { ImageAnalyzer(get()) }
        single { PatternDetectionEngine() }

        // AccessGuard for MemoryAnalysisService
        single {
            GuardFactory.createDefaultGuard(get(), get())
        }

        // Memory Analysis Service - create its own AccessGuard to avoid circular dependency
        single {
            MemoryAnalysisService(
                imageAnalyzer = get(),
                patternDetectionEngine = get(),
                guard = get(),
                userPlanManager = get()
            )
        }

        // User Session Manager - moved from DataModule to avoid circular dependency
        single<UserSessionManager> {
            UserSessionManagerImpl(get(), get())
        }

        // Local Storage Manager
        single {
            LocalStorageManager(
                memoryRepository = get(),
                affirmationRepository = get(),
                userPreferencesStorage = get(),
                sessionDataStore = get(),
                crashlyticsManager = get()
            )
        }
    }
}