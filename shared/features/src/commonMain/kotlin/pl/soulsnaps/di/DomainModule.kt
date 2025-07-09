package pl.soulsnaps.di

import org.koin.core.module.Module
import org.koin.core.module.dsl.factoryOf
import org.koin.core.qualifier.named
import org.koin.dsl.module
import pl.soulsnaps.data.RuleBasedAffirmationGenerator
import pl.soulsnaps.domain.AffirmationGenerator
import pl.soulsnaps.domain.interactor.GenerateAffirmationUseCase
import pl.soulsnaps.domain.interactor.GetAffirmationsUseCase
import pl.soulsnaps.domain.interactor.GetAllMemoriesUseCase
import pl.soulsnaps.domain.interactor.GetQuoteOfTheDayUseCase
import pl.soulsnaps.domain.interactor.GetSoulSnapUseCase
import pl.soulsnaps.domain.interactor.SaveMemoryUseCase
import pl.soulsnaps.domain.interactor.ToggleFavoriteUseCase

object DomainModule {
    fun get() = module {
        factoryOf(::SaveMemoryUseCase)
        factoryOf(::GetAllMemoriesUseCase)
        factoryOf(::GetAffirmationsUseCase)
        factoryOf(::ToggleFavoriteUseCase)
        factoryOf(::GetQuoteOfTheDayUseCase)
        factoryOf(::GetSoulSnapUseCase)
        single<AffirmationGenerator>(qualifier = named("openai")) {
            RuleBasedAffirmationGenerator()
        }
        single<AffirmationGenerator>(qualifier = named("ruleBased")) {
            RuleBasedAffirmationGenerator()
        }
        //        factoryOf(::SignUpUseCase)
//        factoryOf(::SignInUseCase)
//        factoryOf(::GetUserStatusUseCase)
//        factoryOf(::SignInWithGoogleUseCase)
//        factoryOf(::PlayAffirmationUseCase)
//        factoryOf(::ResetPasswordUseCase)
        factory {
            GenerateAffirmationUseCase(
                openAIGenerator = get(named("openai")),
                ruleBasedGenerator = get(named("ruleBased"))
            )
        }
    }
}
