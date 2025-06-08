package org.drag.me.service

import org.drag.me.data.BuildingBlockData
import org.drag.me.models.BuildingBlockDto
import org.drag.me.models.CreateBuildingBlockRequest
import kotlin.random.Random

class BuildingBlockService {
    
    fun getAllBlocks(): List<BuildingBlockDto> {
        return BuildingBlockData.getAllBlocks()
    }
    
    fun getDefaultBlocks(): List<BuildingBlockDto> {
        return BuildingBlockData.getDefaultBlocks()
    }
    
    fun getCustomBlocks(): List<BuildingBlockDto> {
        return BuildingBlockData.getCustomBlocks()
    }
    
    fun createBlock(request: CreateBuildingBlockRequest): BuildingBlockDto {
        val id = "custom_${Random.nextLong()}"
        val block = BuildingBlockDto(
            id = id,
            name = request.name,
            colorHex = request.colorHex
        )
        return BuildingBlockData.addCustomBlock(block)
    }
    
    fun deleteBlock(id: String): Boolean {
        return BuildingBlockData.deleteBlock(id)
    }
    
    fun getBlockById(id: String): BuildingBlockDto? {
        return BuildingBlockData.getBlockById(id)
    }
}
