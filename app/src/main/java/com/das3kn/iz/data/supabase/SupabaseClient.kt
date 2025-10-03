package com.das3kn.iz.data.supabase

import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.storage.Storage

object SupabaseClient {
    val client = createSupabaseClient(
        supabaseUrl = "https://troybdkwjhvinwfgvuem.supabase.co",
        supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InRyb3liZGt3amh2aW53Zmd2dWVtIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NTkzOTIxNDEsImV4cCI6MjA3NDk2ODE0MX0.pFk9iaXJqUPxAaSfykxXXdQgeTXy6DCuG7nP4BON3Ws" // Supabase Dashboard'dan gerçek anon key'i kopyalayın
    ) {
        install(Postgrest)
        install(Storage)
    }
}
