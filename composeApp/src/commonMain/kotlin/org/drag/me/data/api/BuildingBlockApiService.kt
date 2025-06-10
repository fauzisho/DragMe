package org.drag.me.data.api

import org.drag.me.api.BuildingBlockApiClient
import org.drag.me.models.BuildingBlockDto
import org.drag.me.models.CreateBuildingBlockRequest

/**
 * Updated API service that uses Supabase instead of HTTP
 * This maintains the same interface but uses Supabase under the hood
 */
class BuildingBlockApiService {
    
    private val supabaseApiClient = BuildingBlockApiClient()
    
    suspend fun getAllBlocks(): Result<List<BuildingBlockDto>> {
        return try {
            println("🔄 Fetching blocks from Supabase...")
            val response = supabaseApiClient.getAllBlocks()
            
            println("📦 Supabase Response - Success: ${response.success}")
            if (response.success) {
                val data = response.data
                if (data != null) {
                    println("✅ Got ${data.blocks.size} blocks from Supabase")
                    Result.success(data.blocks)
                } else {
                    println("❌ Supabase returned success but no data")
                    Result.failure(Exception("No data received"))
                }
            } else {
                println("❌ Supabase returned error: ${response.error}")
                Result.failure(Exception(response.error ?: "Unknown error"))
            }
        } catch (e: Exception) {
            println("💥 Supabase Error: ${e.message}")
            e.printStackTrace()
            // Return empty list as fallback for development
            Result.success(emptyList())
        }
    }
    
    suspend fun createBlock(name: String, colorHex: String): Result<BuildingBlockDto> {
        return try {
            println("🔄 Creating block: $name with color $colorHex")
            val request = CreateBuildingBlockRequest(name, colorHex)
            val response = supabaseApiClient.createBlock(request)
            
            println("📦 Create Supabase Response - Success: ${response.success}")
            if (response.success) {
                val data = response.data
                if (data != null) {
                    println("✅ Block created successfully: ${data.id}")
                    Result.success(data)
                } else {
                    println("❌ Supabase returned success but no data")
                    Result.failure(Exception("No data received"))
                }
            } else {
                println("❌ Supabase returned error: ${response.error}")
                Result.failure(Exception(response.error ?: "Failed to create block"))
            }
        } catch (e: Exception) {
            println("💥 Create Supabase Error: ${e.message}")
            e.printStackTrace()
            Result.failure(e)
        }
    }
    
    suspend fun deleteBlock(id: String): Result<String> {
        return try {
            println("🔄 Deleting block: $id")
            val response = supabaseApiClient.deleteBlock(id)
            
            if (response.success) {
                println("✅ Block deleted successfully")
                Result.success(response.data ?: "Block deleted successfully")
            } else {
                println("❌ Failed to delete block: ${response.error}")
                Result.failure(Exception(response.error ?: "Failed to delete block"))
            }
        } catch (e: Exception) {
            println("💥 Delete Supabase Error: ${e.message}")
            e.printStackTrace()
            Result.failure(e)
        }
    }
}
