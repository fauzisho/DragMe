package org.drag.me.model

import androidx.compose.ui.geometry.Offset

data class Connection(
    val fromBlockId: String,
    val toBlockId: String,
    val fromPosition: Offset,
    val toPosition: Offset
)