package org.drag.me.api

import org.drag.me.models.ApiResponse
import org.drag.me.models.BuildingBlockDto
import org.drag.me.models.BuildingBlocksResponse
import org.drag.me.models.CreateBuildingBlockRequest
import org.drag.me.service.SupabaseBuildingBlockService

/**
 * API Client that uses Supabase instead of HTTP endpoints
 * This replaces the previous HTTP-based API calls with direct Supabase operations
 */
class BuildingBlockApiClient {
    
    private val service = SupabaseBuildingBlockService()
    
    /**
     * Get all building blocks
     */
    suspend fun getAllBlocks(): ApiResponse<BuildingBlocksResponse> {
        return try {
            val blocks = service.getAllBlocks()
            ApiResponse(
                success = true,
                data = BuildingBlocksResponse(blocks)
            )
        } catch (e: Exception) {
            ApiResponse(
                success = false,
                error = "Failed to fetch building blocks: ${e.message}"
            )
        }
    }
    
    /**
     * Create a new building block
     */
    suspend fun createBlock(request: CreateBuildingBlockRequest): ApiResponse<BuildingBlockDto> {
        return try {
            val block = service.createBlock(request)
            ApiResponse(
                success = true,
                data = block
            )
        } catch (e: IllegalArgumentException) {
            ApiResponse(
                success = false,
                error = e.message
            )
        } catch (e: Exception) {
            ApiResponse(
                success = false,
                error = "Failed to create building block: ${e.message}"
            )
        }
    }
    
    /**
     * Delete a building block
     */
    suspend fun deleteBlock(id: String): ApiResponse<String> {
        return try {
            val deleted = service.deleteBlock(id)
            if (deleted) {
                ApiResponse(
                    success = true,
                    data = "Block deleted successfully"
                )
            } else {
                ApiResponse(
                    success = false,
                    error = "Block not found or cannot be deleted (default blocks cannot be deleted)"
                )
            }
        } catch (e: IllegalArgumentException) {
            ApiResponse(
                success = false,
                error = e.message
            )
        } catch (e: Exception) {
            ApiResponse(
                success = false,
                error = "Failed to delete building block: ${e.message}"
            )
        }
    }
    
    /**
     * Get a specific building block by ID
     */
    suspend fun getBlockById(id: String): ApiResponse<BuildingBlockDto> {
        return try {
            val block = service.getBlockById(id)
            if (block != null) {
                ApiResponse(
                    success = true,
                    data = block
                )
            } else {
                ApiResponse(
                    success = false,
                    error = "Block not found"
                )
            }
        } catch (e: IllegalArgumentException) {
            ApiResponse(
                success = false,
                error = e.message
            )
        } catch (e: Exception) {
            ApiResponse(
                success = false,
                error = "Failed to fetch building block: ${e.message}"
            )
        }
    }
    
    /**
     * Initialize default blocks (call this once when setting up)
     */
    suspend fun initializeDefaultBlocks(): ApiResponse<String> {
        return try {
            val initialized = service.initializeDefaultBlocks()
            if (initialized) {
                ApiResponse(
                    success = true,
                    data = "Default blocks initialized successfully"
                )
            } else {
                ApiResponse(
                    success = false,
                    error = "Failed to initialize default blocks"
                )
            }
        } catch (e: Exception) {
            ApiResponse(
                success = false,
                error = "Failed to initialize default blocks: ${e.message}"
            )
        }
    }
}
