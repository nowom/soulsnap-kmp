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
import pl.soulsnaps.domain.interactor.SignInUseCase
import pl.soulsnaps.domain.interactor.RegisterUseCase
import pl.soulsnaps.domain.interactor.SignInAnonymouslyUseCase
import pl.soulsnaps.domain.interactor.SignOutUseCase
import pl.soulsnaps.domain.interactor.GetMemoryByIdUseCase
import pl.soulsnaps.domain.interactor.ToggleMemoryFavoriteUseCase
import pl.soulsnaps.domain.interactor.GetDailyQuizUseCase
import pl.soulsnaps.domain.interactor.SubmitQuizAnswersUseCase
import pl.soulsnaps.domain.interactor.GetQuizSummaryUseCase
import pl.soulsnaps.domain.interactor.GenerateAIReflectionUseCase
import pl.soulsnaps.domain.interactor.DeleteMemoryUseCase
import pl.soulsnaps.domain.interactor.EditMemoryUseCase
import pl.soulsnaps.domain.interactor.SearchLocationsUseCase
import pl.soulsnaps.domain.interactor.ClearUserDataUseCase
import pl.soulsnaps.domain.interactor.ClearDataOnSessionExpiredUseCase
import pl.soulsnaps.domain.interactor.SessionExpirationHandler
import pl.soulsnaps.domain.interactor.UserPlanUseCase
import pl.soulsnaps.domain.interactor.ForceSyncAllMemoriesUseCase

object DomainModule {
    fun get() = module {
        factoryOf(::SaveMemoryUseCase)
        factoryOf(::GetAllMemoriesUseCase)
        factoryOf(::GetAffirmationsUseCase)
        factoryOf(::ToggleFavoriteUseCase)
        factoryOf(::GetQuoteOfTheDayUseCase)
        factoryOf(::GetSoulSnapUseCase)
        factoryOf(::SignInUseCase)
        factoryOf(::RegisterUseCase)
        factoryOf(::SignInAnonymouslyUseCase)
        factory { SignOutUseCase(get(), get(), get()) }
        factoryOf(::GetMemoryByIdUseCase)
        factoryOf(::ToggleMemoryFavoriteUseCase)
        factoryOf(::DeleteMemoryUseCase)
        factoryOf(::EditMemoryUseCase)
        factoryOf(::SearchLocationsUseCase)
        factoryOf(::ClearUserDataUseCase)
        factoryOf(::ClearDataOnSessionExpiredUseCase)
        factoryOf(::SessionExpirationHandler)
        factoryOf(::UserPlanUseCase)
        factoryOf(::ForceSyncAllMemoriesUseCase)
        
        // Daily Quiz Use Cases
        factory { GetDailyQuizUseCase(get()) }
        factory { SubmitQuizAnswersUseCase(get(), get()) }
        factory { GetQuizSummaryUseCase(get()) }
        factory { GenerateAIReflectionUseCase(get()) }
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
