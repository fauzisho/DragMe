package org.drag.me

import androidx.compose.ui.geometry.Offset
import org.drag.me.model.BuildingBlock

data class DragState(
    val isDragging: Boolean = false,
    val draggedBlock: BuildingBlock? = null,
    val dragOffset: Offset = Offset.Zero,
    val initialPosition: Offset = Offset.Zero
)