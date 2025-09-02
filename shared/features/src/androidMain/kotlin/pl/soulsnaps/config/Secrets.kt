package pl.soulsnaps.config

import pl.soulsnaps.features.BuildConfig


/**
 * Android implementation of Secrets
 * Uses BuildConfig to access gradle properties securely
 */
actual object Secrets {
    actual val SUPABASE_URL: String = BuildConfig.SUPABASE_URL
    actual val SUPABASE_ANON_KEY: String = BuildConfig.SUPABASE_ANON_KEY
}