package org.drag.me.api

import org.drag.me.SERVER_PORT

object ApiEndpoints {
    // Production URL - your actual Railway domain
    const val PRODUCTION_URL = "https://dragme-production.up.railway.app"
    const val LOCAL_URL = "http://localhost:$SERVER_PORT"
    
    // Simple approach: Use production URL for now
    // You can manually change this to LOCAL_URL during development
    val BASE_URL = PRODUCTION_URL
    
    const val BUILDING_BLOCKS = "/api/building-blocks"
    const val CREATE_BUILDING_BLOCK = "/api/building-blocks"
    const val DELETE_BUILDING_BLOCK = "/api/building-blocks/{id}"
}
