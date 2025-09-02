package pl.soulsnaps.database

import app.cash.sqldelight.db.SqlDriver

actual class DatabaseDriverFactory {
    actual fun createDriver(): SqlDriver {
        // SQLDelight doesn't have a wasm driver, so this is a stub implementation
        // In a real wasm app, you might want to use IndexedDB or another storage solution
        throw UnsupportedOperationException("SQLDelight is not supported in WebAssembly")
    }
}


