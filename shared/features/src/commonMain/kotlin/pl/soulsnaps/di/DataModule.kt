package pl.soulsnaps.di

import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import pl.soulsnaps.data.AffirmationRepositoryImpl
import pl.soulsnaps.data.FakeQuoteRepository
import pl.soulsnaps.data.MemoryRepositoryImpl
import pl.soulsnaps.domain.AffirmationRepository
import pl.soulsnaps.domain.MemoryRepository
import pl.soulsnaps.domain.QuoteRepository

object DataModule {
    fun get() = module {
        singleOf(::AffirmationRepositoryImpl) { bind<AffirmationRepository>()}
        single<MemoryRepository> {  MemoryRepositoryImpl(get()) }
        single<QuoteRepository> { FakeQuoteRepository() }
    }
}