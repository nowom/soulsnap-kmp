package pl.soulsnaps.config

object AuthConfig {
    // Set this to true to use Supabase authentication and database, false to use fake data
    const val USE_SUPABASE_AUTH = false
    
    // Supabase configuration
    const val SUPABASE_URL = "https://your-project.supabase.co" // TODO: Replace with your actual Supabase URL
    const val SUPABASE_ANON_KEY = "your-anon-key" // TODO: Replace with your actual anon key
    
    // Email confirmation settings
    const val REQUIRE_EMAIL_CONFIRMATION = false // Set to true if you want to require email confirmation
    
    // Database settings
    const val ENABLE_REAL_TIME_SYNC = true // Enable real-time database sync
    const val CACHE_DURATION_MINUTES = 5L // How long to cache data locally
}
