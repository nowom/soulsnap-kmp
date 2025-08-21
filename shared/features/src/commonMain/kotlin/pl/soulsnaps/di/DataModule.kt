package pl.soulsnaps.di

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
import pl.soulsnaps.database.dao.MemoryDao
import pl.soulsnaps.database.DatabaseModule
import pl.soulsnaps.features.auth.SessionDataStore
import pl.soulsnaps.features.auth.InMemorySessionDataStore
import pl.soulsnaps.features.auth.UserSessionManager
import pl.soulsnaps.features.auth.mvp.guard.PaywallTrigger
import pl.soulsnaps.features.auth.mvp.guard.GuardFactory

object DataModule {
    fun get() = module {
        includes(DatabaseModule.get())
        
        single<AffirmationRepository> { AffirmationRepositoryImpl(get<MemoryDao>()) }

        single<QuoteRepository> { FakeQuoteRepository() }
        single { FakeAuthService() }
        
        // Supabase configuration
        single {
            SupabaseAuthService(
                httpClient = get(),
                supabaseUrl = AuthConfig.SUPABASE_URL,
                supabaseAnonKey = AuthConfig.SUPABASE_ANON_KEY
            )
        }
        
        single {
            SupabaseDatabaseService(
                httpClient = get(),
                supabaseUrl = AuthConfig.SUPABASE_URL,
                supabaseAnonKey = AuthConfig.SUPABASE_ANON_KEY
            )
        }
        
        single<AuthRepository> { 
            if (AuthConfig.USE_SUPABASE_AUTH) {
                SupabaseAuthRepository(get())
            } else {
                AuthRepositoryImpl(get())
            }
        }
        
        single<MemoryRepository> { 
            if (AuthConfig.USE_SUPABASE_AUTH) {
                SupabaseMemoryRepository(get())
            } else {
                MemoryRepositoryImpl(get(), get<MemoryDao>())
            }
        }
        
        // Access Control & Paywall
        single { GuardFactory.createDefaultGuard() }
        single { PaywallTrigger(get(), get()) }
        
        single<SessionDataStore> { InMemorySessionDataStore() }
        single { UserSessionManager(get()) }
        single { HttpClientFactory().create() }

    }
}