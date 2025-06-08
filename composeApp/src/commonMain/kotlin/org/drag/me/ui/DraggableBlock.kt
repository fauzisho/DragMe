package org.drag.me.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.drag.me.model.BuildingBlock


@Composable
fun DraggableBlock(
    block: BuildingBlock,
    isDraggedGlobally: Boolean,
    onDragStart: (Offset) -> Unit,
    onDragEnd: (Offset) -> Unit,
    onDrag: (Offset) -> Unit
) {
    var localDragOffset by remember { mutableStateOf(Offset.Zero) }
    var blockPosition by remember { mutableStateOf(Offset.Zero) }

    val alpha by animateFloatAsState(
        targetValue = if (isDraggedGlobally) 0.3f else 1f,
        label = "alpha"
    )

    Box(
        modifier = Modifier
            .onGloballyPositioned { coordinates ->
                blockPosition = coordinates.localToWindow(Offset.Zero)
            }
            .alpha(alpha)
            .shadow(
                elevation = if (isDraggedGlobally) 1.dp else 2.dp,
                shape = RoundedCornerShape(12.dp)
            )
            .background(
                color = block.color,
                shape = RoundedCornerShape(12.dp)
            )
            .border(
                width = 2.dp,
                color = Color.Transparent,
                shape = RoundedCornerShape(12.dp)
            )
            .size(80.dp, 60.dp)
            .pointerInput(block.id) {
                detectDragGestures(
                    onDragStart = { offset ->
                        localDragOffset = Offset.Zero
                        onDragStart(blockPosition + offset)
                    },
                    onDragEnd = {
                        onDragEnd(blockPosition + localDragOffset)
                        localDragOffset = Offset.Zero
                    },
                    onDrag = { _, dragAmount ->
                        localDragOffset += dragAmount
                        onDrag(localDragOffset)
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = block.name.split(" ")[0],
            color = if (block.color == Color.Yellow || block.color == Color.Cyan) Color.Black else Color.White,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
