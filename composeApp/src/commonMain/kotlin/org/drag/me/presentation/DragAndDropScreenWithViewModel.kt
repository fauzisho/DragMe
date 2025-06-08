package org.drag.me.presentation

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.drag.me.model.*
import org.drag.me.DragState
import org.drag.me.SelectionState
import org.drag.me.extension.drawArrowLine
import org.drag.me.ui.DragOverlay
import org.drag.me.ui.DraggableBlock
import org.drag.me.ui.DroppedBlockItem
import org.drag.me.presentation.components.AddBlockDialog
import org.drag.me.presentation.components.CustomBlocksManager
import org.drag.me.presentation.viewmodel.DragAndDropViewModel
import org.drag.me.presentation.viewmodel.ViewModelFactory

@Composable
fun DragAndDropScreenWithViewModel(
    viewModelFactory: ViewModelFactory
) {
    val viewModel: DragAndDropViewModel = remember { 
        viewModelFactory.create(DragAndDropViewModel::class)
    }
    val uiState by viewModel.uiState.collectAsState()
    
    var dropZoneSize by remember { mutableStateOf(androidx.compose.ui.geometry.Size.Zero) }
    var dropZonePosition by remember { mutableStateOf(Offset.Zero) }
    
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF5F5F5))
                .padding(16.dp)
        ) {
            // Title
            Text(
                text = "🎨 Interactive Connected Blocks",
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
                        items(uiState.buildingBlocks) { block ->
                            DraggableBlock(
                                block = block,
                                isDraggedGlobally = uiState.dragState.draggedBlock?.id == block.id,
                                onDragStart = { initialPos ->
                                    viewModel.setDragState(
                                        DragState(
                                            isDragging = true,
                                            draggedBlock = block,
                                            initialPosition = initialPos
                                        )
                                    )
                                },
                                onDragEnd = { finalOffset ->
                                    uiState.dragState.draggedBlock?.let { dragged ->
                                        val dropZoneRelativeY = finalOffset.y - dropZonePosition.y
                                        
                                        if (dropZoneRelativeY > 0 && dropZoneRelativeY < dropZoneSize.height) {
                                            val dropZoneRelativeX = finalOffset.x - dropZonePosition.x
                                            
                                            // Account for zoom and pan when placing blocks
                                            val adjustedX = (dropZoneRelativeX - uiState.zoomState.offsetX) / uiState.zoomState.scale
                                            val adjustedY = (dropZoneRelativeY - uiState.zoomState.offsetY) / uiState.zoomState.scale
                                            
                                            val clampedX = adjustedX.coerceIn(0f, dropZoneSize.width - 80f)
                                            val clampedY = adjustedY.coerceIn(0f, dropZoneSize.height - 60f)
                                            
                                            val newBlock = DroppedBlock(
                                                block = dragged,
                                                position = Offset(clampedX, clampedY),
                                                order = uiState.nextOrder
                                            )
                                            
                                            viewModel.addDroppedBlock(newBlock)
                                        }
                                    }
                                    viewModel.setDragState(DragState())
                                },
                                onDrag = { offset ->
                                    viewModel.setDragState(
                                        uiState.dragState.copy(dragOffset = offset)
                                    )
                                }
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Custom blocks section
            val customBlocks = uiState.buildingBlocks.filter { block: BuildingBlock ->
                block.id.startsWith("custom_")
            }
            
            CustomBlocksManager(
                customBlocks = customBlocks,
                onAddClick = { viewModel.showAddBlockDialog() },
                onDeleteBlock = { blockId -> viewModel.deleteCustomBlock(blockId) }
            )
            
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
                                val newScale = (uiState.zoomState.scale * zoom).coerceIn(0.5f, 3f)
                                val newOffsetX = uiState.zoomState.offsetX + pan.x
                                val newOffsetY = uiState.zoomState.offsetY + pan.y
                                
                                viewModel.setZoomState(
                                    ZoomState(
                                        scale = newScale,
                                        offsetX = newOffsetX,
                                        offsetY = newOffsetY
                                    )
                                )
                            }
                        }
                ) {
                    // Zoomable content
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer(
                                scaleX = uiState.zoomState.scale,
                                scaleY = uiState.zoomState.scale,
                                translationX = uiState.zoomState.offsetX,
                                translationY = uiState.zoomState.offsetY
                            )
                    ) {
                        // Draw connections
                        Canvas(
                            modifier = Modifier.fillMaxSize()
                        ) {
                            uiState.connections.forEach { connection: Connection ->
                                drawArrowLine(
                                    start = connection.fromPosition,
                                    end = connection.toPosition,
                                    color = Color(0xFF3498DB),
                                    strokeWidth = 4.dp.toPx()
                                )
                            }
                        }
                        
                        // Drop zone instructions
                        if (uiState.droppedBlocks.isEmpty()) {
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
                            }
                        }
                        
                        // Render dropped blocks
                        uiState.droppedBlocks.forEach { droppedBlock: DroppedBlock ->
                            DroppedBlockItem(
                                droppedBlock = droppedBlock,
                                isDraggedGlobally = uiState.dragState.draggedBlock?.id == droppedBlock.block.id,
                                isSelected = uiState.selectionState.selectedBlockId == droppedBlock.id,
                                isOtherSelected = uiState.selectionState.isSelecting && uiState.selectionState.selectedBlockId != droppedBlock.id,
                                dropZoneSize = dropZoneSize,
                                onSingleClick = {
                                    when {
                                        uiState.selectionState.selectedBlockId == null -> {
                                            viewModel.setSelectionState(
                                                SelectionState(
                                                    selectedBlockId = droppedBlock.id,
                                                    isSelecting = true
                                                )
                                            )
                                        }
                                        uiState.selectionState.selectedBlockId == droppedBlock.id -> {
                                            viewModel.setSelectionState(SelectionState())
                                        }
                                        else -> {
                                            viewModel.createManualConnection(
                                                uiState.selectionState.selectedBlockId!!,
                                                droppedBlock.id
                                            )
                                            viewModel.setSelectionState(SelectionState())
                                        }
                                    }
                                },
                                onDoubleClick = {
                                    viewModel.removeDroppedBlock(droppedBlock.id)
                                },
                                onPositionUpdate = { newPosition ->
                                    viewModel.updateDroppedBlockPosition(droppedBlock.id, newPosition)
                                },
                                onDragStart = { _ -> },
                                onDragEnd = { },
                                onDrag = { _ -> }
                            )
                        }
                    }
                }
            }
            
            // Clear button
            if (uiState.droppedBlocks.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = { viewModel.clearAll() },
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE74C3C))
                ) {
                    Text("🗑️ Clear All", color = Color.White)
                }
            }
        }
        
        // Global drag overlay
        if (uiState.dragState.isDragging && uiState.dragState.draggedBlock != null) {
            DragOverlay(
                block = uiState.dragState.draggedBlock!!,
                offset = uiState.dragState.initialPosition + uiState.dragState.dragOffset
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
        uiState.errorMessage?.let { error: String ->
            Snackbar(
                modifier = Modifier.align(Alignment.BottomCenter),
                action = {
                    TextButton(onClick = { viewModel.clearError() }) {
                        Text("Dismiss", color = Color.White)
                    }
                }
            ) {
                Text(text = error)
            }
        }
    }
}