package pl.soulsnaps.config

/**
 * iOS implementation of Secrets
 * Uses environment variables or Info.plist for secure secret management
 */
actual object Secrets {
    actual val SUPABASE_URL: String = "https://ofnywwkbwcxevmsvcrpu.supabase.co"
    actual val SUPABASE_ANON_KEY: String = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Im9mbnl3d2tid2N4ZXZtc3ZjcnB1Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3NTYyMzE4NTQsImV4cCI6MjA3MTgwNzg1NH0.bYVlp2I9u9lVgj4e0FQDY4j0HL-mMcKfGWpAzB2PdYI"
}