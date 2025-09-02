package pl.soulsnaps.network

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.storage.Storage
import io.github.jan.supabase.realtime.Realtime
import pl.soulsnaps.config.Secrets

object SupabaseClientProvider {
    
    private val supabaseClient: SupabaseClient by lazy {
        createSupabaseClient(
            supabaseUrl = Secrets.SUPABASE_URL,
            supabaseKey = Secrets.SUPABASE_ANON_KEY
        ) {
            install(Auth) {
                // Auth configuration
            }
            install(Postgrest) {
                // Postgrest configuration
            }
            install(Storage) {
                // Storage configuration
            }
            install(Realtime) {
                // Realtime configuration
            }
        }
    }
    
    fun getClient(): SupabaseClient = supabaseClient
}
