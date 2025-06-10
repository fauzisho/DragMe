package org.drag.me.examples

import org.drag.me.api.BuildingBlockApiClient
import org.drag.me.models.CreateBuildingBlockRequest

/**
 * Example of how to use the new Supabase-based API
 * Replace your old HTTP API calls with these methods
 */
class SupabaseUsageExample {
    
    private val apiClient = BuildingBlockApiClient()
    
    /**
     * Example: Get all building blocks
     * This replaces your old HTTP GET to /api/building-blocks
     */
    suspend fun getAllBlocksExample() {
        val response = apiClient.getAllBlocks()
        if (response.success) {
            val blocks = response.data?.blocks ?: emptyList()
            println("Fetched ${blocks.size} blocks")
            blocks.forEach { block ->
                println("Block: ${block.name} (${block.colorHex})")
            }
        } else {
            println("Error: ${response.error}")
        }
    }
    
    /**
     * Example: Create a new building block
     * This replaces your old HTTP POST to /api/building-blocks
     */
    suspend fun createBlockExample() {
        val request = CreateBuildingBlockRequest(
            name = "Custom Purple",
            colorHex = "#9932CC"
        )
        
        val response = apiClient.createBlock(request)
        if (response.success) {
            val block = response.data
            println("Created block: ${block?.name} with ID: ${block?.id}")
        } else {
            println("Error creating block: ${response.error}")
        }
    }
    
    /**
     * Example: Delete a building block
     * This replaces your old HTTP DELETE to /api/building-blocks/{id}
     */
    suspend fun deleteBlockExample(blockId: String) {
        val response = apiClient.deleteBlock(blockId)
        if (response.success) {
            println("Block deleted successfully")
        } else {
            println("Error deleting block: ${response.error}")
        }
    }
    
    /**
     * Example: Get a specific building block
     * This replaces your old HTTP GET to /api/building-blocks/{id}
     */
    suspend fun getBlockByIdExample(blockId: String) {
        val response = apiClient.getBlockById(blockId)
        if (response.success) {
            val block = response.data
            println("Found block: ${block?.name} (${block?.colorHex})")
        } else {
            println("Error fetching block: ${response.error}")
        }
    }
    
    /**
     * Example: Initialize default blocks (call this once when setting up)
     */
    suspend fun initializeDefaultBlocksExample() {
        val response = apiClient.initializeDefaultBlocks()
        if (response.success) {
            println("Default blocks initialized successfully")
        } else {
            println("Error initializing default blocks: ${response.error}")
        }
    }
}
