package org.drag.me.data.api

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import org.drag.me.api.ApiEndpoints
import org.drag.me.models.ApiResponse
import org.drag.me.models.BuildingBlockDto
import org.drag.me.models.BuildingBlocksResponse
import org.drag.me.models.CreateBuildingBlockRequest

class BuildingBlockApiService(
    private val client: HttpClient = createHttpClient()
) {
    
    suspend fun getAllBlocks(): Result<List<BuildingBlockDto>> {
        return try {
            println("🔄 Making API call to: ${ApiEndpoints.BASE_URL}${ApiEndpoints.BUILDING_BLOCKS}")
            val response: ApiResponse<BuildingBlocksResponse> = client.get("${ApiEndpoints.BASE_URL}${ApiEndpoints.BUILDING_BLOCKS}").body()
            
            println("📦 API Response - Success: ${response.success}")
            if (response.success) {
                val data = response.data
                if (data != null) {
                    println("✅ Got ${data.blocks.size} blocks from API")
                    Result.success(data.blocks)
                } else {
                    println("❌ API returned success but no data")
                    Result.failure(Exception("No data received"))
                }
            } else {
                println("❌ API returned error: ${response.error}")
                Result.failure(Exception(response.error ?: "Unknown error"))
            }
        } catch (e: Exception) {
            println("💥 API Error: ${e.message}")
            e.printStackTrace()
            // Return empty list as fallback for development
            Result.success(emptyList())
        }
    }
    
    suspend fun createBlock(name: String, colorHex: String): Result<BuildingBlockDto> {
        return try {
            println("🔄 Creating block: $name with color $colorHex")
            val request = CreateBuildingBlockRequest(name, colorHex)
            val response: ApiResponse<BuildingBlockDto> = client.post("${ApiEndpoints.BASE_URL}${ApiEndpoints.CREATE_BUILDING_BLOCK}") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }.body()
            
            println("📦 Create API Response - Success: ${response.success}")
            if (response.success) {
                val data = response.data
                if (data != null) {
                    println("✅ Block created successfully: ${data.id}")
                    Result.success(data)
                } else {
                    println("❌ API returned success but no data")
                    Result.failure(Exception("No data received"))
                }
            } else {
                println("❌ API returned error: ${response.error}")
                Result.failure(Exception(response.error ?: "Failed to create block"))
            }
        } catch (e: Exception) {
            println("💥 Create API Error: ${e.message}")
            e.printStackTrace()
            Result.failure(e)
        }
    }
    
    suspend fun deleteBlock(id: String): Result<String> {
        return try {
            val response: ApiResponse<String> = client.delete("${ApiEndpoints.BASE_URL}${ApiEndpoints.DELETE_BUILDING_BLOCK}".replace("{id}", id)).body()
            
            if (response.success) {
                Result.success(response.data ?: "Block deleted successfully")
            } else {
                Result.failure(Exception(response.error ?: "Failed to delete block"))
            }
        } catch (e: Exception) {
            println("API Error: ${e.message}")
            Result.failure(e)
        }
    }
}
