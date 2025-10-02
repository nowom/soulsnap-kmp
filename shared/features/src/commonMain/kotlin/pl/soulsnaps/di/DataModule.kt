package pl.soulsnaps.di

import io.github.jan.supabase.SupabaseClient
import org.koin.core.module.Module
import org.koin.dsl.module
import pl.soulsnaps.access.guard.GuardFactory
import pl.soulsnaps.data.AffirmationRepositoryImpl
import pl.soulsnaps.data.AnalyticsRepository
import pl.soulsnaps.data.FakeAnalyticsRepository
import pl.soulsnaps.data.FakeQuoteRepository
import pl.soulsnaps.data.MemoryRepositoryImpl
import pl.soulsnaps.data.OnlineDataSource
import pl.soulsnaps.data.SupabaseAuthRepository
import pl.soulsnaps.data.SupabaseMemoryDataSource
import pl.soulsnaps.data.UserPlanRepositoryImpl
import pl.soulsnaps.domain.AffirmationRepository
import pl.soulsnaps.domain.AuthRepository
import pl.soulsnaps.domain.MemoryRepository
import pl.soulsnaps.domain.QuoteRepository
import pl.soulsnaps.domain.UserPlanRepository
import pl.soulsnaps.domain.service.AffirmationService
import pl.soulsnaps.domain.service.AffirmationServiceImpl
import pl.soulsnaps.storage.FileStorageManager
import pl.soulsnaps.domain.StartupRepository
import pl.soulsnaps.data.StartupRepositoryImpl
import pl.soulsnaps.domain.MemoryMaintenance
import pl.soulsnaps.domain.NoOpMemoryMaintenance
// import pl.soulsnaps.features.auth.InMemorySessionDataStore // Removed - using PersistentSessionDataStore
import pl.soulsnaps.features.auth.PersistentSessionDataStore
import pl.soulsnaps.features.auth.SessionDataStore
import pl.soulsnaps.features.auth.UserSessionManager
import pl.soulsnaps.features.auth.UserSessionManagerImpl
import pl.soulsnaps.network.SupabaseAuthService
import pl.soulsnaps.network.SupabaseClientProvider

object DataModule {
    fun get(): Module = module {
        single<SupabaseClient> { SupabaseClientProvider.getClient() }
        single { SupabaseAuthService(get()) }
        single<OnlineDataSource> { SupabaseMemoryDataSource(get<SupabaseClient>(), get(), get(), get(), get()) }


        // Analytics Repository
        single<AnalyticsRepository> { FakeAnalyticsRepository() }
        
        single<AuthRepository> {
            SupabaseAuthRepository(get())
        }
        
        // CapacityGuard - singleton for capacity management
        single { GuardFactory.createCapacityGuard(get(), get()) }

        
        single<MemoryRepository> {
            MemoryRepositoryImpl(
                get(),
                get(),
                get(),
                get<OnlineDataSource>(),
                get(),
                get<pl.soulsnaps.sync.manager.SyncManager>(),
                get<FileStorageManager>()
            )
        }
        
        single<AffirmationRepository> { AffirmationRepositoryImpl(get()) }
        single<QuoteRepository> { FakeQuoteRepository() }
        
        // MemoryMaintenance - DIP with default NoOp implementation
        single<MemoryMaintenance> { NoOpMemoryMaintenance() }
        
        // StartupRepository - new startup flow
        single<StartupRepository> {
            StartupRepositoryImpl(
                userPlanManager = get(),
                onboardingManager = get(),
                authService = get(),
                memoryMaintenance = get()
            )
        }
        
        // Affirmation Service
        single<AffirmationService> { AffirmationServiceImpl() }
        
        // Session management - PERSISTENT SESSION STORAGE
        single<SessionDataStore> { 
            PersistentSessionDataStore(get())
        }

        // User Plan Repository
        single<UserPlanRepository> {
            UserPlanRepositoryImpl(get())
        }
    }
}