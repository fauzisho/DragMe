package org.drag.me

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import kotlin.math.*
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.drag.me.model.*
import org.drag.me.extension.drawArrowLine
import org.drag.me.ui.DragOverlay
import org.drag.me.ui.DraggableBlock
import org.drag.me.ui.DroppedBlockItem
import org.drag.me.presentation.viewmodel.ViewModelFactory
import org.drag.me.presentation.components.AddBlockDialog

@Composable
@Preview
fun App() {
    MaterialTheme {
        DragAndDropScreenWithViewModel()
    }
}

@Composable
expect fun AppWithViewModel()

@Composable
fun AppContent(viewModelFactory: ViewModelFactory) {
    MaterialTheme {
        DragAndDropScreenWithViewModel()
    }
}

@Composable
fun DragAndDropScreenWithViewModel() {
    // Use ViewModel for managing building blocks
    val viewModelFactory = ViewModelFactory()
    val viewModel = remember { viewModelFactory.create(org.drag.me.presentation.viewmodel.DragAndDropViewModel::class) }
    val uiState by viewModel.uiState.collectAsState()
    
    // Local state for drag and drop functionality
    var droppedBlocks by remember { mutableStateOf<List<DroppedBlock>>(emptyList()) }
    var connections by remember { mutableStateOf<List<Connection>>(emptyList()) }
    var dragState by remember { mutableStateOf(DragState()) }
    var selectionState by remember { mutableStateOf(SelectionState()) }
    var dropZoneSize by remember { mutableStateOf(androidx.compose.ui.geometry.Size.Zero) }
    var dropZonePosition by remember { mutableStateOf(Offset.Zero) }
    var nextOrder by remember { mutableStateOf(0) }
    var zoomState by remember { mutableStateOf(ZoomState()) }
    
    // Function to calculate block center position
    val getBlockCenter = { block: DroppedBlock ->
        Offset(
            block.position.x + 40f, // Half of block width (80dp / 2)
            block.position.y + 30f  // Half of block height (60dp / 2)
        )
    }
    
    // Function to create connection between blocks
    val createConnection = { fromBlock: DroppedBlock, toBlock: DroppedBlock ->
        val fromCenter = getBlockCenter(fromBlock)
        val toCenter = getBlockCenter(toBlock)
        Connection(
            fromBlockId = fromBlock.id,
            toBlockId = toBlock.id,
            fromPosition = fromCenter,
            toPosition = toCenter
        )
    }
    
    // Function to create manual connection between two blocks
    val createManualConnection = { fromBlockId: String, toBlockId: String ->
        val fromBlock = droppedBlocks.find { it.id == fromBlockId }
        val toBlock = droppedBlocks.find { it.id == toBlockId }
        
        if (fromBlock != null && toBlock != null) {
            // Check if connection already exists
            val connectionExists = connections.any { 
                (it.fromBlockId == fromBlockId && it.toBlockId == toBlockId) ||
                (it.fromBlockId == toBlockId && it.toBlockId == fromBlockId)
            }
            
            if (!connectionExists) {
                val newConnection = createConnection(fromBlock, toBlock)
                connections = connections + newConnection
            }
        }
    }
    
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF5F5F5))
                .padding(16.dp)
        ) {
            // Title
            Text(
                text = "🎨 Interactive Connected Blocks with Custom Blocks",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2C3E50),
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            // Building blocks palette (now using ViewModel)
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "🧱 Building Blocks (${uiState.buildingBlocks.size})",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF34495E)
                        )
                        
                        // Add custom block button
                        Button(
                            onClick = { viewModel.showAddBlockDialog() },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF27AE60)),
                            modifier = Modifier.height(32.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp)
                        ) {
                            Text("+ Add", color = Color.White, fontSize = 12.sp)
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(16.dp), // Increased spacing
                        contentPadding = PaddingValues(horizontal = 4.dp) // Add padding for delete buttons
                    ) {
                        items(uiState.buildingBlocks) { block ->
                            Box(
                                modifier = Modifier.padding(8.dp) // Add padding around each block
                            ) {
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
                                            val dropZoneRelativeY = finalOffset.y - dropZonePosition.y
                                            
                                            if (dropZoneRelativeY > 0 && dropZoneRelativeY < dropZoneSize.height) {
                                                val dropZoneRelativeX = finalOffset.x - dropZonePosition.x
                                                
                                                // Account for zoom and pan when placing blocks
                                                val adjustedX = (dropZoneRelativeX - zoomState.offsetX) / zoomState.scale
                                                val adjustedY = (dropZoneRelativeY - zoomState.offsetY) / zoomState.scale
                                                
                                                val clampedX = adjustedX.coerceIn(0f, dropZoneSize.width - 80f)
                                                val clampedY = adjustedY.coerceIn(0f, dropZoneSize.height - 60f)
                                                
                                                val newBlock = DroppedBlock(
                                                    block = dragged,
                                                    position = Offset(clampedX, clampedY),
                                                    order = nextOrder
                                                )
                                                
                                                droppedBlocks = droppedBlocks + newBlock
                                                
                                                if (droppedBlocks.isNotEmpty()) {
                                                    val previousBlock = droppedBlocks
                                                        .filter { it.id != newBlock.id }
                                                        .maxByOrNull { it.order }
                                                    previousBlock?.let { prevBlock ->
                                                        val newConnection = createConnection(prevBlock, newBlock)
                                                        connections = connections + newConnection
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
                                
                                // Delete button for custom blocks with better positioning
                                if (block.id.startsWith("custom_")) {
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .offset(x = 4.dp, y = (-4).dp) // Better positioning
                                            .size(24.dp) // Slightly larger for easier tapping
                                            .background(
                                                color = Color(0xFFE74C3C),
                                                shape = CircleShape
                                            )
                                            .border(
                                                width = 2.dp,
                                                color = Color.White,
                                                shape = CircleShape
                                            )
                                            .clickable { viewModel.deleteCustomBlock(block.id) },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "×",
                                            color = Color.White,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Drop zone with zoom functionality
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
                        .pointerInput(Unit) {
                            detectTransformGestures(
                                panZoomLock = false
                            ) { _, pan, zoom, _ ->
                                val newScale = (zoomState.scale * zoom).coerceIn(0.5f, 3f)
                                val newOffsetX = zoomState.offsetX + pan.x
                                val newOffsetY = zoomState.offsetY + pan.y
                                
                                zoomState = ZoomState(
                                    scale = newScale,
                                    offsetX = newOffsetX,
                                    offsetY = newOffsetY
                                )
                            }
                        }
                ) {
                    // Zoomable content
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer(
                                scaleX = zoomState.scale,
                                scaleY = zoomState.scale,
                                translationX = zoomState.offsetX,
                                translationY = zoomState.offsetY
                            )
                    ) {
                        // Draw connections
                        Canvas(
                            modifier = Modifier.fillMaxSize()
                        ) {
                            connections.forEach { connection ->
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
                                    text = "Interactive Drop Zone",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF7F8C8D)
                                )
                                Text(
                                    text = "Drop blocks to auto-connect",
                                    fontSize = 14.sp,
                                    color = Color(0xFFBDC3C7)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "👆 Single click: Connect blocks",
                                    fontSize = 12.sp,
                                    color = Color(0xFF95A5A6)
                                )
                                Text(
                                    text = "👆👆 Double click: Delete blocks",
                                    fontSize = 12.sp,
                                    color = Color(0xFF95A5A6)
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "🔍 Pinch to zoom • 👆 Drag to pan",
                                    fontSize = 12.sp,
                                    color = Color(0xFF95A5A6),
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                        
                        // Render dropped blocks
                        droppedBlocks.forEach { droppedBlock ->
                            DroppedBlockItem(
                                droppedBlock = droppedBlock,
                                isDraggedGlobally = dragState.draggedBlock?.id == droppedBlock.block.id,
                                isSelected = selectionState.selectedBlockId == droppedBlock.id,
                                isOtherSelected = selectionState.isSelecting && selectionState.selectedBlockId != droppedBlock.id,
                                dropZoneSize = dropZoneSize,
                                onSingleClick = {
                                    if (selectionState.selectedBlockId == null) {
                                        // First selection
                                        selectionState = SelectionState(
                                            selectedBlockId = droppedBlock.id,
                                            isSelecting = true
                                        )
                                    } else if (selectionState.selectedBlockId == droppedBlock.id) {
                                        // Clicking the same block - deselect
                                        selectionState = SelectionState()
                                    } else {
                                        // Second selection - create connection
                                        createManualConnection(selectionState.selectedBlockId!!, droppedBlock.id)
                                        selectionState = SelectionState()
                                    }
                                },
                                onDoubleClick = {
                                    // Double click to remove
                                    val blockId = droppedBlock.id
                                    droppedBlocks = droppedBlocks.filter { it.id != blockId }
                                    connections = connections.filter { 
                                        it.fromBlockId != blockId && it.toBlockId != blockId 
                                    }
                                    // Clear selection if deleted block was selected
                                    if (selectionState.selectedBlockId == blockId) {
                                        selectionState = SelectionState()
                                    }
                                },
                                onPositionUpdate = { newPosition ->
                                    droppedBlocks = droppedBlocks.map { block ->
                                        if (block.id == droppedBlock.id) {
                                            block.copy(position = newPosition)
                                        } else {
                                            block
                                        }
                                    }
                                    
                                    // Update connections with proper center calculation
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
                    }
                    
                    // Zoom controls
                    Row(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FloatingActionButton(
                            onClick = {
                                zoomState = ZoomState(
                                    scale = (zoomState.scale * 1.2f).coerceIn(0.5f, 3f),
                                    offsetX = zoomState.offsetX,
                                    offsetY = zoomState.offsetY
                                )
                            },
                            modifier = Modifier.size(40.dp),
                            containerColor = Color(0xFF3498DB)
                        ) {
                            Text("+", color = Color.White, fontSize = 18.sp)
                        }
                        
                        FloatingActionButton(
                            onClick = {
                                zoomState = ZoomState(
                                    scale = (zoomState.scale / 1.2f).coerceIn(0.5f, 3f),
                                    offsetX = zoomState.offsetX,
                                    offsetY = zoomState.offsetY
                                )
                            },
                            modifier = Modifier.size(40.dp),
                            containerColor = Color(0xFF3498DB)
                        ) {
                            Text("-", color = Color.White, fontSize = 18.sp)
                        }
                        
                        FloatingActionButton(
                            onClick = {
                                zoomState = ZoomState() // Reset to default
                            },
                            modifier = Modifier.size(40.dp),
                            containerColor = Color(0xFF95A5A6)
                        ) {
                            Text("⌂", color = Color.White, fontSize = 16.sp)
                        }
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
                                Text(
                                    text = "Available: ${uiState.buildingBlocks.size}",
                                    fontSize = 10.sp,
                                    color = Color(0xFF27AE60)
                                )
                                Text(
                                    text = "Zoom: ${(zoomState.scale * 100).toInt()}%",
                                    fontSize = 10.sp,
                                    color = Color(0xFF27AE60)
                                )
                                if (selectionState.isSelecting) {
                                    Text(
                                        text = "Click to connect",
                                        fontSize = 9.sp,
                                        color = Color(0xFFE67E22),
                                        fontWeight = FontWeight.Bold
                                    )
                                }
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
                        selectionState = SelectionState()
                    },
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE74C3C))
                ) {
                    Text("🗑️ Clear All", color = Color.White)
                }
            }
        }
        
        // Global drag overlay
        if (dragState.isDragging && dragState.draggedBlock != null) {
            DragOverlay(
                block = dragState.draggedBlock!!,
                offset = dragState.initialPosition + dragState.dragOffset
            )
        }
        
        // Add block dialog
        if (uiState.showAddBlockDialog) {
            AddBlockDialog(
                onDismiss = { viewModel.hideAddBlockDialog() },
                onConfirm = { name, color -> 
                    viewModel.addCustomBlock(name, color)
                },
                isLoading = uiState.isLoading
            )
        }
        
        // Error handling
        uiState.errorMessage?.let { error ->
            Card(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE74C3C).copy(alpha = 0.9f))
            ) {
                Text(
                    text = "Error: $error",
                    color = Color.White,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }
}
