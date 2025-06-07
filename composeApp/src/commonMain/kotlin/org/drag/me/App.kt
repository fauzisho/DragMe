package org.drag.me

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import kotlin.math.roundToInt
import org.jetbrains.compose.ui.tooling.preview.Preview

data class BuildingBlock(
    val id: String,
    val color: Color,
    val name: String
)

data class DroppedBlock(
    val block: BuildingBlock,
    val position: Offset,
    val id: String = "${block.id}_${block.id.hashCode()}" // Using hashCode for a more deterministic ID, consider a UUID for true uniqueness
)

@Composable
@Preview
fun App() {
    MaterialTheme {
        DragAndDropScreen()
    }
}

@Composable
fun DragAndDropScreen() {
    val buildingBlocks = remember {
        listOf(
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
    
    var droppedBlocks by remember { mutableStateOf<List<DroppedBlock>>(emptyList()) }
    var draggedBlock by remember { mutableStateOf<BuildingBlock?>(null) }
    var dragOffset by remember { mutableStateOf(Offset.Zero) }
    var dropZoneSize by remember { mutableStateOf(androidx.compose.ui.geometry.Size.Zero) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
            .padding(16.dp)
    ) {
        // Title
        Text(
            text = "🎨 Drag & Drop Building Blocks",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF2C3E50),
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        // Building blocks palette
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "🧱 Building Blocks",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF34495E),
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(buildingBlocks) { block ->
                        DraggableBlock(
                            block = block,
                            onDragStart = { draggedBlock = block },
                            onDragEnd = { offset ->
                                draggedBlock?.let { dragged ->
                                    // Check if dropped in valid area
                                    if (offset.y > 200f) { // Simple drop zone check
                                        droppedBlocks = droppedBlocks + DroppedBlock(
                                            block = dragged,
                                            position = offset
                                        )
                                    }
                                }
                                draggedBlock = null
                                dragOffset = Offset.Zero
                            },
                            onDrag = { offset ->
                                dragOffset = offset
                            }
                        )
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Drop zone
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .onGloballyPositioned { coordinates ->
                    dropZoneSize = androidx.compose.ui.geometry.Size(
                        coordinates.size.width.toFloat(),
                        coordinates.size.height.toFloat()
                    )
                },
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFAFAFA))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Drop zone instructions
                if (droppedBlocks.isEmpty()) {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "🎯",
                            fontSize = 48.sp
                        )
                        Text(
                            text = "Drop Zone",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF7F8C8D)
                        )
                        Text(
                            text = "Drag blocks here to build!",
                            fontSize = 14.sp,
                            color = Color(0xFFBDC3C7)
                        )
                    }
                }
                
                // Render dropped blocks
                droppedBlocks.forEach { droppedBlock ->
                    DroppedBlockItem(
                        droppedBlock = droppedBlock,
                        onRemove = {
                            droppedBlocks = droppedBlocks.filter { it.id != droppedBlock.id }
                        }
                    )
                }
                
                // Stats
                if (droppedBlocks.isNotEmpty()) {
                    Card(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFECF0F1))
                    ) {
                        Text(
                            text = "Blocks: ${droppedBlocks.size}",
                            modifier = Modifier.padding(8.dp),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF2C3E50)
                        )
                    }
                }
            }
        }
        
        // Clear button
        if (droppedBlocks.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = { droppedBlocks = emptyList() },
                modifier = Modifier.align(Alignment.CenterHorizontally),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE74C3C))
            ) {
                Text("🗑️ Clear All", color = Color.White)
            }
        }
    }
}

@Composable
fun DraggableBlock(
    block: BuildingBlock,
    onDragStart: () -> Unit,
    onDragEnd: (Offset) -> Unit,
    onDrag: (Offset) -> Unit
) {
    var isDragging by remember { mutableStateOf(false) }
    var dragOffset by remember { mutableStateOf(Offset.Zero) }
    
    val alpha by animateFloatAsState(
        targetValue = if (isDragging) 0.7f else 1f,
        label = "alpha"
    )
    
    Box(
        modifier = Modifier
            .offset {
                IntOffset(
                    dragOffset.x.roundToInt(),
                    dragOffset.y.roundToInt()
                )
            }
            .alpha(alpha)
            .shadow(
                elevation = if (isDragging) 8.dp else 2.dp,
                shape = RoundedCornerShape(12.dp)
            )
            .background(
                color = block.color,
                shape = RoundedCornerShape(12.dp)
            )
            .border(
                width = 2.dp,
                color = if (isDragging) Color.White else Color.Transparent,
                shape = RoundedCornerShape(12.dp)
            )
            .size(80.dp, 60.dp)
            .pointerInput(block.id) {
                detectDragGestures(
                    onDragStart = {
                        isDragging = true
                        onDragStart()
                    },
                    onDragEnd = {
                        isDragging = false
                        onDragEnd(dragOffset)
                        dragOffset = Offset.Zero
                    },
                    onDrag = { change, dragAmount ->
                        dragOffset += dragAmount
                        onDrag(dragOffset)
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

@Composable
fun DroppedBlockItem(
    droppedBlock: DroppedBlock,
    onRemove: () -> Unit
) {
    Box(
        modifier = Modifier
            .offset {
                IntOffset(
                    droppedBlock.position.x.roundToInt(),
                    droppedBlock.position.y.roundToInt()
                )
            }
            .shadow(4.dp, RoundedCornerShape(12.dp))
            .background(
                color = droppedBlock.block.color,
                shape = RoundedCornerShape(12.dp)
            )
            .border(
                width = 1.dp,
                color = Color(0xFFBDC3C7),
                shape = RoundedCornerShape(12.dp)
            )
            .size(80.dp, 60.dp)
            .clickable { onRemove() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = droppedBlock.block.name.split(" ")[0],
            color = if (droppedBlock.block.color == Color.Yellow || droppedBlock.block.color == Color.Cyan) Color.Black else Color.White,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
