package org.drag.me.repository

import com.benasher44.uuid.uuid4
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import org.drag.me.models.*
import org.drag.me.supabase.SupabaseConfig

class BuildingBlockRepository {
    
    private val supabase = SupabaseConfig.client
    private val tableName = "building_blocks"
    
    /**
     * Get all building blocks from Supabase
     */
    suspend fun getAllBlocks(): List<BuildingBlockDto> {
        return try {
            val entities = supabase
                .from(tableName)
                .select()
                .decodeList<BuildingBlockEntity>()
            
            entities.map { it.toDto() }
        } catch (e: Exception) {
            println("Error fetching blocks: ${e.message}")
            getDefaultBlocks() // Fallback to default blocks
        }
    }
    
    /**
     * Get only default building blocks
     */
    suspend fun getDefaultBlocks(): List<BuildingBlockDto> {
        return try {
            val entities = supabase
                .from(tableName)
                .select {
                    filter {
                        eq("is_default", true)
                    }
                }
                .decodeList<BuildingBlockEntity>()
            
            entities.map { it.toDto() }
        } catch (e: Exception) {
            println("Error fetching default blocks: ${e.message}")
            // Return hardcoded defaults as fallback
            listOf(
                BuildingBlockDto("red", "Red Block", "#FF0000"),
                BuildingBlockDto("blue", "Blue Block", "#0000FF"),
                BuildingBlockDto("green", "Green Block", "#00FF00"),
                BuildingBlockDto("yellow", "Yellow Block", "#FFFF00"),
                BuildingBlockDto("purple", "Purple Block", "#FF00FF"),
                BuildingBlockDto("orange", "Orange Block", "#FF8000"),
                BuildingBlockDto("cyan", "Cyan Block", "#00FFFF"),
                BuildingBlockDto("pink", "Pink Block", "#FFB3D9")
            )
        }
    }
    
    /**
     * Get only custom (non-default) building blocks
     */
    suspend fun getCustomBlocks(): List<BuildingBlockDto> {
        return try {
            val entities = supabase
                .from(tableName)
                .select {
                    filter {
                        eq("is_default", false)
                    }
                }
                .decodeList<BuildingBlockEntity>()
            
            entities.map { it.toDto() }
        } catch (e: Exception) {
            println("Error fetching custom blocks: ${e.message}")
            emptyList()
        }
    }
    
    /**
     * Create a new building block
     */
    suspend fun createBlock(request: CreateBuildingBlockRequest): BuildingBlockDto {
        return try {
            val entity = CreateBuildingBlockEntity(
                name = request.name,
                colorHex = request.colorHex,
                isDefault = false
            )
            
            val createdEntity = supabase
                .from(tableName)
                .insert(entity) {
                    select()
                }
                .decodeSingle<BuildingBlockEntity>()
            
            createdEntity.toDto()
        } catch (e: Exception) {
            println("Error creating block: ${e.message}")
            throw Exception("Failed to create building block: ${e.message}")
        }
    }
    
    /**
     * Delete a building block by ID
     */
    suspend fun deleteBlock(id: String): Boolean {
        return try {
            // First check if it's a default block
            val block = getBlockById(id)
            if (block == null) {
                return false
            }
            
            // Check if it's a default block by querying the database
            val entity = supabase
                .from(tableName)
                .select(Columns.list("is_default")) {
                    filter {
                        eq("id", id)
                    }
                }
                .decodeSingleOrNull<BuildingBlockEntity>()
            
            if (entity?.isDefault == true) {
                return false // Don't allow deletion of default blocks
            }
            
            supabase
                .from(tableName)
                .delete {
                    filter {
                        eq("id", id)
                        eq("is_default", false) // Double-check we're not deleting defaults
                    }
                }
            
            true
        } catch (e: Exception) {
            println("Error deleting block: ${e.message}")
            false
        }
    }
    
    /**
     * Get a specific building block by ID
     */
    suspend fun getBlockById(id: String): BuildingBlockDto? {
        return try {
            val entity = supabase
                .from(tableName)
                .select {
                    filter {
                        eq("id", id)
                    }
                }
                .decodeSingleOrNull<BuildingBlockEntity>()
            
            entity?.toDto()
        } catch (e: Exception) {
            println("Error fetching block by ID: ${e.message}")
            null
        }
    }
    
    /**
     * Initialize default blocks if they don't exist
     * Call this once when setting up your Supabase database
     */
    suspend fun initializeDefaultBlocks(): Boolean {
        return try {
            val defaultBlocks = listOf(
                mapOf(
                    "id" to "red",
                    "name" to "Red Block",
                    "color_hex" to "#FF0000",
                    "is_default" to true
                ),
                mapOf(
                    "id" to "blue",
                    "name" to "Blue Block",
                    "color_hex" to "#0000FF",
                    "is_default" to true
                ),
                mapOf(
                    "id" to "green",
                    "name" to "Green Block",
                    "color_hex" to "#00FF00",
                    "is_default" to true
                ),
                mapOf(
                    "id" to "yellow",
                    "name" to "Yellow Block",
                    "color_hex" to "#FFFF00",
                    "is_default" to true
                ),
                mapOf(
                    "id" to "purple",
                    "name" to "Purple Block",
                    "color_hex" to "#FF00FF",
                    "is_default" to true
                ),
                mapOf(
                    "id" to "orange",
                    "name" to "Orange Block",
                    "color_hex" to "#FF8000",
                    "is_default" to true
                ),
                mapOf(
                    "id" to "cyan",
                    "name" to "Cyan Block",
                    "color_hex" to "#00FFFF",
                    "is_default" to true
                ),
                mapOf(
                    "id" to "pink",
                    "name" to "Pink Block",
                    "color_hex" to "#FFB3D9",
                    "is_default" to true
                )
            )
            
            // Check if any default blocks already exist
            val existingDefaults = supabase
                .from(tableName)
                .select(Columns.list("id")) {
                    filter {
                        eq("is_default", true)
                    }
                }
                .decodeList<BuildingBlockEntity>()
            
            if (existingDefaults.isEmpty()) {
                // Insert default blocks one by one to ensure proper IDs
                defaultBlocks.forEach { blockData ->
                    try {
                        supabase
                            .from(tableName)
                            .insert(blockData)
                    } catch (e: Exception) {
                        println("Warning: Could not insert default block ${blockData["name"]}: ${e.message}")
                    }
                }
                
                println("✅ Default building blocks initialized")
            } else {
                println("ℹ️  Default building blocks already exist")
            }
            
            true
        } catch (e: Exception) {
            println("Error initializing default blocks: ${e.message}")
            false
        }
    }
}
