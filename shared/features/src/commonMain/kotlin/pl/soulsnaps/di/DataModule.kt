package pl.soulsnaps.di

import org.koin.core.module.Module
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import pl.soulsnaps.data.AffirmationRepositoryImpl
import pl.soulsnaps.data.AuthRepositoryImpl
import pl.soulsnaps.data.FakeQuoteRepository
import pl.soulsnaps.data.FakeAuthService
import pl.soulsnaps.data.MemoryRepositoryImpl
import pl.soulsnaps.data.SupabaseAuthRepository
import pl.soulsnaps.data.SupabaseMemoryRepository
import pl.soulsnaps.network.SupabaseAuthService
import pl.soulsnaps.network.SupabaseDatabaseService
import pl.soulsnaps.config.AuthConfig
import pl.soulsnaps.domain.AuthRepository
import pl.soulsnaps.network.HttpClientFactory
import pl.soulsnaps.domain.AffirmationRepository
import pl.soulsnaps.domain.MemoryRepository
import pl.soulsnaps.domain.QuoteRepository
import pl.soulsnaps.data.AnalyticsRepository
import pl.soulsnaps.data.FakeAnalyticsRepository
import pl.soulsnaps.features.auth.UserSessionManager
import pl.soulsnaps.features.auth.SessionDataStore
import pl.soulsnaps.features.auth.InMemorySessionDataStore
import pl.soulsnaps.network.SupabaseClientProvider

object DataModule {
    fun get(): Module = module {
        single { HttpClientFactory() }
        single { SupabaseClientProvider.getClient() }
        single { SupabaseAuthService() }
        single { SupabaseDatabaseService(get()) }


        // Analytics Repository
        single<AnalyticsRepository> { FakeAnalyticsRepository() }
        
        single<AuthRepository> {
            if (AuthConfig.USE_SUPABASE_AUTH) {
                SupabaseAuthRepository(get())
            } else {
                AuthRepositoryImpl(FakeAuthService())
            }
        }
        
        single<MemoryRepository> {
            if (AuthConfig.USE_SUPABASE_AUTH) {
                SupabaseMemoryRepository(get(), get())
            } else {
                MemoryRepositoryImpl(get(), get(), get())
            }
        }
        
        single<AffirmationRepository> { AffirmationRepositoryImpl(get()) }
        single<QuoteRepository> { FakeQuoteRepository() }
        
        // Session management
        single<SessionDataStore> { InMemorySessionDataStore() }
        single { UserSessionManager(get()) }
    }
}