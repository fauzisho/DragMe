package org.drag.me.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import org.drag.me.model.BuildingBlock
import kotlin.math.roundToInt


@Composable
fun DragOverlay(
    block: BuildingBlock,
    offset: Offset
) {
    Box(
        modifier = Modifier
            .offset {
                IntOffset(
                    (offset.x - 40.dp.value).roundToInt(),
                    (offset.y - 30.dp.value).roundToInt()
                )
            }
            .zIndex(1000f)
            .shadow(12.dp, RoundedCornerShape(12.dp))
            .background(
                color = block.color,
                shape = RoundedCornerShape(12.dp)
            )
            .border(
                width = 3.dp,
                color = Color.White,
                shape = RoundedCornerShape(12.dp)
            )
            .size(80.dp, 60.dp),
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
