package com.laguipemo.nefroped.core.data.supabase

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.storage.Storage

fun createSupabaseClient(): SupabaseClient {
    return createSupabaseClient(
        supabaseUrl = "https://dokkkyeyixyyurjophor.supabase.co",
        supabaseKey = "sb_publishable_tySdxMCRHeUFs_umUqc9nQ_5cTrAdeE"
    ) {
        install(Auth) {
            // Esto ayuda al SDK a construir las URLs de redirección por defecto
            scheme = "nefroped"
            host = "reset-password"
        }
        install(Postgrest)
        install(Realtime)
        install(Storage)
    }
}