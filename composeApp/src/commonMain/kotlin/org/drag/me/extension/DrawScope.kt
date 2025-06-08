package org.drag.me.extension

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

fun androidx.compose.ui.graphics.drawscope.DrawScope.drawArrowLine(
    start: Offset,
    end: Offset,
    color: Color,
    strokeWidth: Float
) {
    // Draw the main line
    drawLine(
        color = color,
        start = start,
        end = end,
        strokeWidth = strokeWidth
    )

    // Calculate arrow head
    val arrowLength = 15f
    val arrowAngle = PI / 4 // 45 degrees

    val lineAngle = atan2(end.y - start.y, end.x - start.x)

    val arrowEnd1 = Offset(
        end.x - arrowLength * cos(lineAngle - arrowAngle).toFloat(),
        end.y - arrowLength * sin(lineAngle - arrowAngle).toFloat()
    )

    val arrowEnd2 = Offset(
        end.x - arrowLength * cos(lineAngle + arrowAngle).toFloat(),
        end.y - arrowLength * sin(lineAngle + arrowAngle).toFloat()
    )

    // Draw arrow head lines
    drawLine(
        color = color,
        start = end,
        end = arrowEnd1,
        strokeWidth = strokeWidth
    )

    drawLine(
        color = color,
        start = end,
        end = arrowEnd2,
        strokeWidth = strokeWidth
    )
}