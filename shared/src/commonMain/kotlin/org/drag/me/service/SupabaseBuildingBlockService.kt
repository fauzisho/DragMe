package org.drag.me.service

import org.drag.me.models.BuildingBlockDto
import org.drag.me.models.CreateBuildingBlockRequest
import org.drag.me.repository.BuildingBlockRepository

class SupabaseBuildingBlockService {
    
    private val repository = BuildingBlockRepository()
    
    suspend fun getAllBlocks(): List<BuildingBlockDto> {
        return repository.getAllBlocks()
    }
    
    suspend fun getDefaultBlocks(): List<BuildingBlockDto> {
        return repository.getDefaultBlocks()
    }
    
    suspend fun getCustomBlocks(): List<BuildingBlockDto> {
        return repository.getCustomBlocks()
    }
    
    suspend fun createBlock(request: CreateBuildingBlockRequest): BuildingBlockDto {
        // Validate request
        if (request.name.isBlank()) {
            throw IllegalArgumentException("Block name cannot be empty")
        }
        
        // Validate hex color format
        if (!request.colorHex.matches(Regex("^#[0-9A-Fa-f]{6}$"))) {
            throw IllegalArgumentException("Invalid color format. Use hex format like #FF0000")
        }
        
        return repository.createBlock(request)
    }
    
    suspend fun deleteBlock(id: String): Boolean {
        if (id.isBlank()) {
            throw IllegalArgumentException("Block ID cannot be empty")
        }
        
        return repository.deleteBlock(id)
    }
    
    suspend fun getBlockById(id: String): BuildingBlockDto? {
        if (id.isBlank()) {
            throw IllegalArgumentException("Block ID cannot be empty")
        }
        
        return repository.getBlockById(id)
    }
    
    suspend fun initializeDefaultBlocks(): Boolean {
        return repository.initializeDefaultBlocks()
    }
}
