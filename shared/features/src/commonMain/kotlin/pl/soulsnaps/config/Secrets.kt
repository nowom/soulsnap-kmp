package pl.soulsnaps.config

/**
 * Secrets configuration using expect/actual pattern for Kotlin Multiplatform
 * This allows platform-specific implementations for secure secret management
 */
expect object Secrets {
    val SUPABASE_URL: String
    val SUPABASE_ANON_KEY: String
}