package org.drag.me.model

import androidx.compose.ui.geometry.Offset

data class DroppedBlock(
    val block: BuildingBlock,
    val position: Offset,
    val id: String = "${block.id}_${kotlin.random.Random.nextLong()}",
    val order: Int
)