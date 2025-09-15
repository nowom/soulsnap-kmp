package pl.soulsnaps.di

import org.koin.dsl.module
import pl.soulsnaps.domain.service.AffirmationService
import pl.soulsnaps.domain.service.AffirmationServiceImpl

val affirmationModule = module {
    single<AffirmationService> { AffirmationServiceImpl() }
}
