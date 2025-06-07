package org.drag.me

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import kotlin.math.*
import org.jetbrains.compose.ui.tooling.preview.Preview

data class BuildingBlock(
    val id: String,
    val color: Color,
    val name: String
)

data class DroppedBlock(
    val block: BuildingBlock,
    val position: Offset,
    val id: String = "${block.id}_${kotlin.random.Random.nextLong()}",
    val order: Int // Track the order of placement for connections
)

data class Connection(
    val fromBlockId: String,
    val toBlockId: String,
    val fromPosition: Offset,
    val toPosition: Offset
)

data class DragState(
    val isDragging: Boolean = false,
    val draggedBlock: BuildingBlock? = null,
    val dragOffset: Offset = Offset.Zero,
    val initialPosition: Offset = Offset.Zero
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
    var connections by remember { mutableStateOf<List<Connection>>(emptyList()) }
    var dragState by remember { mutableStateOf(DragState()) }
    var dropZoneSize by remember { mutableStateOf(androidx.compose.ui.geometry.Size.Zero) }
    var dropZonePosition by remember { mutableStateOf(Offset.Zero) }
    var nextOrder by remember { mutableStateOf(0) }
    
    // Function to calculate block center position
    val getBlockCenter = { block: DroppedBlock ->
        Offset(
            block.position.x + 40f, // Half of block width (80dp)
            block.position.y + 30f  // Half of block height (60dp)
        )
    }
    
    // Function to create connection between blocks
    val createConnection = { fromBlock: DroppedBlock, toBlock: DroppedBlock ->
        val fromCenter = getBlockCenter(fromBlock)
        val toCenter = getBlockCenter(toBlock)
        println("Creating connection from (${fromCenter.x}, ${fromCenter.y}) to (${toCenter.x}, ${toCenter.y})")
        Connection(
            fromBlockId = fromBlock.id,
            toBlockId = toBlock.id,
            fromPosition = fromCenter,
            toPosition = toCenter
        )
    }
    
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF5F5F5))
                .padding(16.dp)
        ) {
            // Title
            Text(
                text = "🎨 Connected Building Blocks",
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
                                isDraggedGlobally = dragState.draggedBlock?.id == block.id,
                                onDragStart = { initialPos ->
                                    dragState = DragState(
                                        isDragging = true,
                                        draggedBlock = block,
                                        initialPosition = initialPos
                                    )
                                },
                                onDragEnd = { finalOffset ->
                                    dragState.draggedBlock?.let { dragged ->
                                        // Calculate final position relative to drop zone
                                        val dropZoneRelativeY = finalOffset.y - dropZonePosition.y
                                        
                                        // Check if dropped in valid area (drop zone)
                                        if (dropZoneRelativeY > 0 && dropZoneRelativeY < dropZoneSize.height) {
                                            val dropZoneRelativeX = finalOffset.x - dropZonePosition.x
                                            
                                            // Clamp position within drop zone bounds
                                            val clampedX = dropZoneRelativeX.coerceIn(0f, dropZoneSize.width - 80f)
                                            val clampedY = dropZoneRelativeY.coerceIn(0f, dropZoneSize.height - 60f)
                                            
                                            val newBlock = DroppedBlock(
                                                block = dragged,
                                                position = Offset(clampedX, clampedY),
                                                order = nextOrder
                                            )
                                            
                                            // Add the new block
                                            droppedBlocks = droppedBlocks + newBlock
                                            
                                            // Create connection to the most recently dropped block (if any)
                                            if (droppedBlocks.size >= 1) { // Changed from > 1 to >= 1
                                                val previousBlock = droppedBlocks
                                                    .filter { it.id != newBlock.id }
                                                    .maxByOrNull { it.order }
                                                previousBlock?.let { prevBlock ->
                                                    println("Connecting ${prevBlock.block.name} to ${newBlock.block.name}")
                                                    val newConnection = createConnection(prevBlock, newBlock)
                                                    connections = connections + newConnection
                                                    println("Total connections: ${connections.size}")
                                                }
                                            }
                                            
                                            nextOrder++
                                        }
                                    }
                                    dragState = DragState()
                                },
                                onDrag = { offset ->
                                    dragState = dragState.copy(dragOffset = offset)
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
                        dropZonePosition = Offset(
                            coordinates.localToWindow(Offset.Zero).x,
                            coordinates.localToWindow(Offset.Zero).y
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
                    // Draw connections (above everything else)
                    Canvas(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        // Draw a test line to verify Canvas is working
                        if (connections.isEmpty() && droppedBlocks.isNotEmpty()) {
                            drawLine(
                                color = Color.Red,
                                start = Offset(50f, 50f),
                                end = Offset(150f, 150f),
                                strokeWidth = 3.dp.toPx()
                            )
                        }
                        
                        // Draw actual connections
                        println("Drawing ${connections.size} connections")
                        connections.forEach { connection ->
                            println("Drawing arrow from (${connection.fromPosition.x}, ${connection.fromPosition.y}) to (${connection.toPosition.x}, ${connection.toPosition.y})")
                            drawArrowLine(
                                start = connection.fromPosition,
                                end = connection.toPosition,
                                color = Color(0xFF3498DB),
                                strokeWidth = 4.dp.toPx()
                            )
                        }
                    }
                    
                    // Drop zone instructions
                    if (droppedBlocks.isEmpty()) {
                        Column(
                            modifier = Modifier.align(Alignment.Center),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "🔗",
                                fontSize = 48.sp
                            )
                            Text(
                                text = "Connected Drop Zone",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF7F8C8D)
                            )
                            Text(
                                text = "Blocks will connect in order!",
                                fontSize = 14.sp,
                                color = Color(0xFFBDC3C7)
                            )
                        }
                    }
                    
                    // Render dropped blocks
                    droppedBlocks.forEach { droppedBlock ->
                        DroppedBlockItem(
                            droppedBlock = droppedBlock,
                            isDraggedGlobally = dragState.draggedBlock?.id == droppedBlock.block.id,
                            dropZoneSize = dropZoneSize,
                            onRemove = {
                                // Remove the block and its connections
                                val blockId = droppedBlock.id
                                droppedBlocks = droppedBlocks.filter { it.id != blockId }
                                connections = connections.filter { 
                                    it.fromBlockId != blockId && it.toBlockId != blockId 
                                }
                            },
                            onPositionUpdate = { newPosition ->
                                // Update block position
                                droppedBlocks = droppedBlocks.map { block ->
                                    if (block.id == droppedBlock.id) {
                                        block.copy(position = newPosition)
                                    } else {
                                        block
                                    }
                                }
                                
                                // Update connections involving this block
                                val updatedBlockCenter = Offset(
                                    newPosition.x + 40f,
                                    newPosition.y + 30f
                                )
                                
                                connections = connections.map { connection ->
                                    when {
                                        connection.fromBlockId == droppedBlock.id -> {
                                            connection.copy(fromPosition = updatedBlockCenter)
                                        }
                                        connection.toBlockId == droppedBlock.id -> {
                                            connection.copy(toPosition = updatedBlockCenter)
                                        }
                                        else -> connection
                                    }
                                }
                            },
                            onDragStart = { initialPos ->
                                dragState = DragState(
                                    isDragging = true,
                                    draggedBlock = droppedBlock.block,
                                    initialPosition = initialPos
                                )
                            },
                            onDragEnd = {
                                dragState = DragState()
                            },
                            onDrag = { offset ->
                                dragState = dragState.copy(dragOffset = offset)
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
                            Column(
                                modifier = Modifier.padding(8.dp)
                            ) {
                                Text(
                                    text = "Blocks: ${droppedBlocks.size}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF2C3E50)
                                )
                                Text(
                                    text = "Connections: ${connections.size}",
                                    fontSize = 10.sp,
                                    color = Color(0xFF3498DB)
                                )
                            }
                        }
                    }
                }
            }
            
            // Clear button
            if (droppedBlocks.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = { 
                        droppedBlocks = emptyList()
                        connections = emptyList()
                        nextOrder = 0
                    },
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE74C3C))
                ) {
                    Text("🗑️ Clear All", color = Color.White)
                }
            }
        }
        
        // Global drag overlay - this renders the dragged block above everything
        if (dragState.isDragging && dragState.draggedBlock != null) {
            DragOverlay(
                block = dragState.draggedBlock!!,
                offset = dragState.initialPosition + dragState.dragOffset
            )
        }
    }
}

// Extension function to draw arrow lines
fun DrawScope.drawArrowLine(
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
    val arrowAngle = PI / 4 // 45 degrees for more visible arrow
    
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

@Composable
fun DroppedBlockItem(
    droppedBlock: DroppedBlock,
    isDraggedGlobally: Boolean,
    dropZoneSize: androidx.compose.ui.geometry.Size,
    onRemove: () -> Unit,
    onPositionUpdate: (Offset) -> Unit,
    onDragStart: (Offset) -> Unit,
    onDragEnd: () -> Unit,
    onDrag: (Offset) -> Unit
) {
    var localDragOffset by remember { mutableStateOf(Offset.Zero) }
    var blockPosition by remember { mutableStateOf(Offset.Zero) }
    var isDragging by remember { mutableStateOf(false) }
    
    val alpha by animateFloatAsState(
        targetValue = if (isDraggedGlobally) 0.3f else 1f,
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
                elevation = if (isDragging) 8.dp else 4.dp,
                shape = RoundedCornerShape(12.dp)
            )
            .background(
                color = droppedBlock.block.color,
                shape = RoundedCornerShape(12.dp)
            )
            .border(
                width = if (isDragging) 2.dp else 1.dp,
                color = if (isDragging) Color.White else Color(0xFFBDC3C7),
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
                        
                        // Calculate final position and clamp within bounds
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
                        
                        // Update connections in real-time during drag
                        val currentPosition = droppedBlock.position + localDragOffset
                        val clampedX = currentPosition.x.coerceIn(0f, dropZoneSize.width - 80f)
                        val clampedY = currentPosition.y.coerceIn(0f, dropZoneSize.height - 60f)
                        val tempPosition = Offset(clampedX, clampedY)
                        
                        onPositionUpdate(tempPosition)
                    }
                )
            }
            .clickable { 
                if (!isDragging) onRemove() // Only allow removal when not dragging
            },
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
