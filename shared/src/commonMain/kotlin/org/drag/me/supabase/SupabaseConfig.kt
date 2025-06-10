package org.drag.me.supabase

import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.Realtime

object SupabaseConfig {
    // You need to replace these with your actual Supabase project URL and anon key
    // Get these from your Supabase project dashboard at https://supabase.com/dashboard
    const val SUPABASE_URL = "https://sfywmsigewgfturdxfem.supabase.co" // e.g., "https://your-project-id.supabase.co"
    const val SUPABASE_ANON_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InNmeXdtc2lnZXdnZnR1cmR4ZmVtIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NDk1NzI4OTEsImV4cCI6MjA2NTE0ODg5MX0.fcq0UeTKt4Y0S2CN3Ye-av3hFu6Y19LsGsjnUvVAWNE" // Your public anon key
    
    val client = createSupabaseClient(
        supabaseUrl = SUPABASE_URL,
        supabaseKey = SUPABASE_ANON_KEY
    ) {
        install(Postgrest)
        install(Auth)
        install(Realtime)
    }
}
