package org.drag.me.api

import org.drag.me.SERVER_PORT

object ApiEndpoints {
    // Production URL - replace with your actual Railway domain after deployment
    const val PRODUCTION_URL = "https://dragme-api.up.railway.app"
    const val LOCAL_URL = "http://localhost:$SERVER_PORT"
    
    // For now, use a simple environment detection
    // You can manually switch this for production builds
    val BASE_URL = LOCAL_URL // Change to PRODUCTION_URL when deployed
    
    const val BUILDING_BLOCKS = "/api/building-blocks"
    const val CREATE_BUILDING_BLOCK = "/api/building-blocks"
    const val DELETE_BUILDING_BLOCK = "/api/building-blocks/{id}"
}
