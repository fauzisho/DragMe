package org.drag.me.data.repository

import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import org.drag.me.model.BuildingBlock

/**
 * In-memory implementation of BuildingBlockRepository for testing and fallback
 */
class InMemoryBuildingBlockRepository : BuildingBlockRepository {
    
    private val defaultBlocks = listOf(
        BuildingBlock("red", Color.Red, "Red Block"),
        BuildingBlock("blue", Color.Blue, "Blue Block"),
        BuildingBlock("green", Color.Green, "Green Block"),
        BuildingBlock("yellow", Color.Yellow, "Yellow Block"),
        BuildingBlock("purple", Color.Magenta, "Purple Block"),
        BuildingBlock("orange", Color.hsl(30f, 1f, 0.5f), "Orange Block"),
        BuildingBlock("cyan", Color.Cyan, "Cyan Block"),
        BuildingBlock("pink", Color.hsl(330f, 0.7f, 0.8f), "Pink Block")
    )
    
    private val customBlocksFlow = MutableStateFlow<List<BuildingBlock>>(emptyList())
    
    override fun getAllBlocks(): Flow<List<BuildingBlock>> {
        return customBlocksFlow.map { customBlocks ->
            defaultBlocks + customBlocks
        }
    }
    
    override fun getCustomBlocks(): Flow<List<BuildingBlock>> = customBlocksFlow
    
    override suspend fun addCustomBlock(name: String, color: Color): Result<Unit> {
        return try {
            val id = "custom_${kotlin.random.Random.nextLong()}"
            val newBlock = BuildingBlock(id, color, name)
            val currentBlocks = customBlocksFlow.value
            customBlocksFlow.value = currentBlocks + newBlock
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun deleteBlock(blockId: String): Result<Unit> {
        return try {
            // Don't allow deletion of default blocks
            if (defaultBlocks.any { it.id == blockId }) {
                return Result.failure(Exception("Cannot delete default blocks"))
            }
            
            val currentBlocks = customBlocksFlow.value
            customBlocksFlow.value = currentBlocks.filter { it.id != blockId }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override fun getDefaultBlocks(): List<BuildingBlock> = defaultBlocks
}
