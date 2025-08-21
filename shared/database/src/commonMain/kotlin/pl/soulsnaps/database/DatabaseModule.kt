package pl.soulsnaps.database

import org.koin.dsl.module
import pl.soulsnaps.database.dao.MemoryDao
import pl.soulsnaps.database.dao.MemoryDaoImpl
import app.cash.sqldelight.db.SqlDriver

object DatabaseModule {
    fun get() = module {
        // SqlDriver is provided by platformModule (Android/iOS)
        single {
            SoulSnapDatabase(get<SqlDriver>()) // Register SoulSnapDatabase
        }
        single<MemoryDao> {
            MemoryDaoImpl(get()) // Now this 'get()' will resolve SoulSnapDatabase
        }
    }
}