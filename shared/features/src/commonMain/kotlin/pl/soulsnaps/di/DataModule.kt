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
import pl.soulsnaps.domain.AffirmationRepository
import pl.soulsnaps.domain.AuthRepository
import pl.soulsnaps.domain.MemoryRepository
import pl.soulsnaps.domain.QuoteRepository
import pl.soulsnaps.domain.service.AffirmationService
import pl.soulsnaps.domain.service.AffirmationServiceImpl
import pl.soulsnaps.features.auth.InMemorySessionDataStore
import pl.soulsnaps.features.auth.SessionDataStore
import pl.soulsnaps.features.auth.UserSessionManager
import pl.soulsnaps.network.SupabaseAuthService
import pl.soulsnaps.network.SupabaseClientProvider

object DataModule {
    fun get(): Module = module {
        single { SupabaseClientProvider.getClient() }
        single { SupabaseAuthService(get()) }
        single<OnlineDataSource> { SupabaseMemoryDataSource(get<SupabaseClient>()) }


        // Analytics Repository
        single<AnalyticsRepository> { FakeAnalyticsRepository() }
        
        single<AuthRepository> {
            SupabaseAuthRepository(get())
        }
        
        // CapacityGuard - singleton for capacity management
        single { GuardFactory.createCapacityGuard(get()) }

        single<MemoryRepository> {
            MemoryRepositoryImpl(
                get(),
                get(),
                get(),
                get<OnlineDataSource>(),
                get(),
                get<pl.soulsnaps.sync.manager.SyncManager>()
            )
        }
        
        single<AffirmationRepository> { AffirmationRepositoryImpl(get()) }
        single<QuoteRepository> { FakeQuoteRepository() }
        
        // Affirmation Service
        single<AffirmationService> { AffirmationServiceImpl() }
        
        // Session management
        single<SessionDataStore> { InMemorySessionDataStore() }
        single { UserSessionManager(get()) }
    }
}