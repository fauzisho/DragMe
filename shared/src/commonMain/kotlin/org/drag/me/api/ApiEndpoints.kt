package org.drag.me.api

object ApiEndpoints {
    // Since we're now using Supabase, we don't need a server URL
    // All API calls will go directly to Supabase
    // Keep these constants for backward compatibility if needed
    const val BASE_URL = "" // No longer needed with Supabase
    
    // These are kept for reference but won't be used with Supabase
    const val BUILDING_BLOCKS = "/api/building-blocks"
    const val CREATE_BUILDING_BLOCK = "/api/building-blocks"
    const val DELETE_BUILDING_BLOCK = "/api/building-blocks/{id}"
}
