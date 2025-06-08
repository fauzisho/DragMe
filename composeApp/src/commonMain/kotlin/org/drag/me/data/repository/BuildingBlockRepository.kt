package org.drag.me.data.repository

import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.flow.Flow
import org.drag.me.model.BuildingBlock

interface BuildingBlockRepository {
    fun getAllBlocks(): Flow<List<BuildingBlock>>
    fun getCustomBlocks(): Flow<List<BuildingBlock>>
    suspend fun addCustomBlock(name: String, color: Color): Result<Unit>
    suspend fun deleteBlock(blockId: String): Result<Unit>
    fun getDefaultBlocks(): List<BuildingBlock>
}
