package org.drag.me.api

object ApiEndpoints {
    // Always use the production Railway URL for both development and production
    // This ensures consistent API access from both local development and GitHub Pages
    const val BASE_URL = "https://dragme-production.up.railway.app"
    
    const val BUILDING_BLOCKS = "/api/building-blocks"
    const val CREATE_BUILDING_BLOCK = "/api/building-blocks"
    const val DELETE_BUILDING_BLOCK = "/api/building-blocks/{id}"
}
