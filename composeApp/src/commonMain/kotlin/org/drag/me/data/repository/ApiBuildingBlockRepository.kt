package org.drag.me.data.repository

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.drag.me.data.api.BuildingBlockApiService
import org.drag.me.model.BuildingBlock
import org.drag.me.models.BuildingBlockDto

/**
 * Repository implementation that fetches data from the backend API
 */
class ApiBuildingBlockRepository(
    private val apiService: BuildingBlockApiService = BuildingBlockApiService()
) : BuildingBlockRepository {
    
    private val blocksFlow = MutableStateFlow<List<BuildingBlock>>(getDefaultBlocks())
    private val repositoryScope = CoroutineScope(Dispatchers.Default)
    
    init {
        // Auto-load blocks from API when repository is created
        repositoryScope.launch {
            loadBlocks()
        }
    }
    
    // Load blocks from API
    suspend fun loadBlocks() {
        println("🔄 Loading blocks from API: ${org.drag.me.api.ApiEndpoints.BASE_URL}")
        val result = apiService.getAllBlocks()
        result.onSuccess { dtoBlocks ->
            println("✅ API returned ${dtoBlocks.size} blocks")
            val blocks = dtoBlocks.map { dto -> dto.toBuildingBlock() }
            blocksFlow.value = blocks
        }.onFailure { error ->
            println("❌ Failed to load blocks from API: ${error.message}")
            // Keep default blocks if API fails
            blocksFlow.value = getDefaultBlocks()
        }
    }
    
    override fun getAllBlocks(): Flow<List<BuildingBlock>> {
        return blocksFlow
    }
    
    override fun getCustomBlocks(): Flow<List<BuildingBlock>> {
        return blocksFlow.map { allBlocks ->
            // Filter out default blocks (assuming they have specific IDs)
            val defaultIds = getDefaultBlocks().map { it.id }.toSet()
            allBlocks.filter { it.id !in defaultIds }
        }
    }
    
    override suspend fun addCustomBlock(name: String, color: Color): Result<Unit> {
        val colorInt = 0xFFFFFF and color.toArgb()
        val colorHex = "#${colorInt.toString(16).padStart(6, '0').uppercase()}"
        
        return try {
            val result = apiService.createBlock(name, colorHex)
            result.onSuccess { dto ->
                // Add the new block to our local state
                val newBlock = dto.toBuildingBlock()
                val currentBlocks = blocksFlow.value
                blocksFlow.value = currentBlocks + newBlock
            }
            result.map { Unit }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun deleteBlock(blockId: String): Result<Unit> {
        return try {
            val result = apiService.deleteBlock(blockId)
            result.onSuccess {
                // Remove the block from our local state
                val currentBlocks = blocksFlow.value
                blocksFlow.value = currentBlocks.filter { it.id != blockId }
            }
            result.map { Unit }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override fun getDefaultBlocks(): List<BuildingBlock> {
        return listOf(
            BuildingBlock("red", Color.Red, "Red Block"),
            BuildingBlock("blue", Color.Blue, "Blue Block"),
            BuildingBlock("green", Color.Green, "Green Block"),
            BuildingBlock("yellow", Color.Yellow, "Yellow Block"),
            BuildingBlock("purple", Color.Magenta, "Purple Block"),
            BuildingBlock("orange", Color.hsl(30f, 1f, 0.5f), "Orange Block"),
            BuildingBlock("cyan", Color.Cyan, "Cyan Block"),
            BuildingBlock("pink", Color.hsl(330f, 0.7f, 0.8f), "Pink Block")
        )
    }
}

// Extension function to convert DTO to domain model
private fun BuildingBlockDto.toBuildingBlock(): BuildingBlock {
    val color = try {
        // Parse hex color to Compose Color
        val colorInt = this.colorHex.removePrefix("#").toLong(16)
        Color(colorInt or 0xFF000000)
    } catch (e: Exception) {
        Color.Gray // Fallback color
    }
    
    return BuildingBlock(
        id = this.id,
        color = color,
        name = this.name
    )
}
