package pl.soulsnaps.config

object AuthConfig {
    // Set this to true to use Supabase authentication and database, false to use fake data
    const val USE_SUPABASE_AUTH = false
    
    // Supabase configuration - safely retrieved using expect/actual pattern
    // This uses platform-specific implementations for secure secret management
    val SUPABASE_URL: String = Secrets.SUPABASE_URL
    val SUPABASE_ANON_KEY: String = Secrets.SUPABASE_ANON_KEY
    
    // Email confirmation settings
    const val REQUIRE_EMAIL_CONFIRMATION = false // Set to true if you want to require email confirmation
    
    // Database settings
    const val ENABLE_REAL_TIME_SYNC = true // Enable real-time database sync
    const val CACHE_DURATION_MINUTES = 5L // Cache duration for offline data
    
    // Security settings
    const val SESSION_TIMEOUT_MINUTES = 60L // Session timeout in minutes
    const val MAX_LOGIN_ATTEMPTS = 5 // Maximum failed login attempts before lockout
    
    // Feature flags
    const val ENABLE_SOCIAL_LOGIN = false // Enable Google, Facebook, etc.
    const val ENABLE_ANONYMOUS_LOGIN = true // Allow users to try app without account
    const val ENABLE_BIOMETRIC_AUTH = false // Enable fingerprint/face ID login
    
    // Development settings
    const val ENABLE_DEBUG_LOGGING = true // Enable detailed logging for development
    const val MOCK_NETWORK_DELAY_MS = 1000L // Simulated network delay for testing
}
