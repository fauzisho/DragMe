package org.drag.me

data class SelectionState(
    val selectedBlockId: String? = null,
    val isSelecting: Boolean = false
)