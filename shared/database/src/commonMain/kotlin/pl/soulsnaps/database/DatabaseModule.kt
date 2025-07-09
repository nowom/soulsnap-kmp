package pl.soulsnaps.database

import org.koin.dsl.module
import pl.soulsnaps.database.dao.MemoryDao
import pl.soulsnaps.database.dao.MemoryDaoImpl

object DatabaseModule {
    fun get() = module {
        single<MemoryDao> {
            MemoryDaoImpl(get())
        }
    }
}