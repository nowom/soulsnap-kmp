package pl.soulsnaps.di

import org.koin.core.context.startKoin
import org.koin.dsl.KoinAppDeclaration
import pl.soulsnaps.database.DatabaseModule

fun initKoin(config: KoinAppDeclaration? = null) {
    startKoin {
        config?.invoke(this)
        modules(
            DatabaseModule.get(),
            DataModule.get(),
            DomainModule.get(),
            FeatureModule.get(),
            platformModule,
        )
    }
}