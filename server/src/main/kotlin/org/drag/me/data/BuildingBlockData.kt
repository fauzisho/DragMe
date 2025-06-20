package org.drag.me.data

import org.drag.me.models.BuildingBlockDto

/**
 * In-memory data store for building blocks
 * In a real application, this would be replaced with a database
 */
object BuildingBlockData {
    private val defaultBlocks = mutableListOf(
        BuildingBlockDto("red", "Red Block", "#FF0000"),
        BuildingBlockDto("blue", "Blue Block", "#0000FF"),
        BuildingBlockDto("green", "Green Block", "#00FF00"),
        BuildingBlockDto("yellow", "Yellow Block", "#FFFF00"),
        BuildingBlockDto("purple", "Purple Block", "#FF00FF"),
        BuildingBlockDto("orange", "Orange Block", "#FF8000"),
        BuildingBlockDto("cyan", "Cyan Block", "#00FFFF"),
        BuildingBlockDto("pink", "Pink Block", "#FFB3D9")
    )
    
    private val customBlocks = mutableListOf<BuildingBlockDto>()
    private const val MAX_CUSTOM_BLOCKS = 100 // Prevent memory leaks
    
    fun getAllBlocks(): List<BuildingBlockDto> {
        return defaultBlocks + customBlocks
    }
    
    fun getDefaultBlocks(): List<BuildingBlockDto> {
        return defaultBlocks.toList()
    }
    
    fun getCustomBlocks(): List<BuildingBlockDto> {
        return customBlocks.toList()
    }
    
    fun addCustomBlock(block: BuildingBlockDto): BuildingBlockDto {
        // Prevent memory leaks by limiting custom blocks
        if (customBlocks.size >= MAX_CUSTOM_BLOCKS) {
            customBlocks.removeFirst() // Remove oldest
        }
        customBlocks.add(block)
        return block
    }
    
    fun deleteBlock(id: String): Boolean {
        // Don't allow deletion of default blocks
        if (defaultBlocks.any { it.id == id }) {
            return false
        }
        return customBlocks.removeIf { it.id == id }
    }
    
    fun getBlockById(id: String): BuildingBlockDto? {
        return getAllBlocks().find { it.id == id }
    }
    
    // Add cleanup method for memory management
    fun cleanup() {
        if (customBlocks.size > MAX_CUSTOM_BLOCKS / 2) {
            val toRemove = customBlocks.size - (MAX_CUSTOM_BLOCKS / 2)
            repeat(toRemove) {
                customBlocks.removeFirstOrNull()
            }
            println("Cleaned up $toRemove custom blocks to prevent memory leak")
        }
    }
}
