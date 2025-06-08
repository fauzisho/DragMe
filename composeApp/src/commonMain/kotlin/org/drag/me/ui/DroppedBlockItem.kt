package org.drag.me.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.drag.me.model.DroppedBlock
import kotlin.math.roundToInt

@Composable
fun DroppedBlockItem(
    droppedBlock: DroppedBlock,
    isDraggedGlobally: Boolean,
    isSelected: Boolean,
    isOtherSelected: Boolean,
    dropZoneSize: androidx.compose.ui.geometry.Size,
    onSingleClick: () -> Unit,
    onDoubleClick: () -> Unit,
    onPositionUpdate: (Offset) -> Unit,
    onDragStart: (Offset) -> Unit,
    onDragEnd: () -> Unit,
    onDrag: (Offset) -> Unit
) {
    var localDragOffset by remember { mutableStateOf(Offset.Zero) }
    var blockPosition by remember { mutableStateOf(Offset.Zero) }
    var isDragging by remember { mutableStateOf(false) }

    val alpha by animateFloatAsState(
        targetValue = when {
            isDraggedGlobally -> 0.3f
            isOtherSelected -> 0.4f
            else -> 1f
        },
        label = "alpha"
    )

    Box(
        modifier = Modifier
            .offset {
                IntOffset(
                    droppedBlock.position.x.roundToInt(),
                    droppedBlock.position.y.roundToInt()
                )
            }
            .onGloballyPositioned { coordinates ->
                blockPosition = coordinates.localToWindow(Offset.Zero)
            }
            .alpha(alpha)
            .shadow(
                elevation = when {
                    isSelected -> 12.dp
                    isDragging -> 8.dp
                    else -> 4.dp
                },
                shape = RoundedCornerShape(12.dp)
            )
            .background(
                color = droppedBlock.block.color,
                shape = RoundedCornerShape(12.dp)
            )
            .border(
                width = if (isSelected) 3.dp else if (isDragging) 2.dp else 1.dp,
                color = when {
                    isSelected -> Color.Yellow
                    isDragging -> Color.White
                    else -> Color(0xFFBDC3C7)
                },
                shape = RoundedCornerShape(12.dp)
            )
            .size(80.dp, 60.dp)
            .pointerInput(droppedBlock.id) {
                detectDragGestures(
                    onDragStart = { offset ->
                        isDragging = true
                        localDragOffset = Offset.Zero
                        onDragStart(blockPosition + offset)
                    },
                    onDragEnd = {
                        isDragging = false

                        val finalPosition = droppedBlock.position + localDragOffset
                        val clampedX = finalPosition.x.coerceIn(0f, dropZoneSize.width - 80f)
                        val clampedY = finalPosition.y.coerceIn(0f, dropZoneSize.height - 60f)
                        val clampedPosition = Offset(clampedX, clampedY)

                        onPositionUpdate(clampedPosition)
                        onDragEnd()
                        localDragOffset = Offset.Zero
                    },
                    onDrag = { _, dragAmount ->
                        localDragOffset += dragAmount
                        onDrag(localDragOffset)

                        val currentPosition = droppedBlock.position + localDragOffset
                        val clampedX = currentPosition.x.coerceIn(0f, dropZoneSize.width - 80f)
                        val clampedY = currentPosition.y.coerceIn(0f, dropZoneSize.height - 60f)
                        val tempPosition = Offset(clampedX, clampedY)

                        onPositionUpdate(tempPosition)
                    }
                )
            }
            .pointerInput(droppedBlock.id) {
                detectTapGestures(
                    onTap = { onSingleClick() },
                    onDoubleTap = { onDoubleClick() }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = droppedBlock.block.name.split(" ")[0],
                color = if (droppedBlock.block.color == Color.Yellow || droppedBlock.block.color == Color.Cyan) Color.Black else Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )

            // Selection indicator
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .background(Color.Yellow, shape = CircleShape)
                        .border(1.dp, Color.White, shape = CircleShape)
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(4.dp)
                        .background(Color.White, shape = CircleShape)
                        .border(1.dp, Color.Black, shape = CircleShape)
                )
            }
        }
    }
}
