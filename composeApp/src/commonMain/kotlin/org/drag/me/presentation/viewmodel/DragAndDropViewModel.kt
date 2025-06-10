package org.drag.me.presentation.viewmodel

import androidx.compose.runtime.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.drag.me.model.*
import org.drag.me.DragState
import org.drag.me.SelectionState
import org.drag.me.data.repository.BuildingBlockRepository

data class DragAndDropUiState(
    val buildingBlocks: List<BuildingBlock> = emptyList(),
    val droppedBlocks: List<DroppedBlock> = emptyList(),
    val connections: List<Connection> = emptyList(),
    val selectionState: SelectionState = SelectionState(),
    val dragState: DragState = DragState(),
    val zoomState: ZoomState = ZoomState(),
    val nextOrder: Int = 0,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val showAddBlockDialog: Boolean = false
)

class DragAndDropViewModel(
    private val repository: BuildingBlockRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(DragAndDropUiState())
    val uiState: StateFlow<DragAndDropUiState> = _uiState.asStateFlow()
    
    init {
        // Load building blocks immediately when repository is created
        loadBuildingBlocks()
    }
    
    private fun loadBuildingBlocks() {
        viewModelScope.launch {
            repository.getAllBlocks()
                .catch { e ->
                    _uiState.update { it.copy(errorMessage = e.message) }
                }
                .collect { blocks ->
                    _uiState.update { it.copy(buildingBlocks = blocks) }
                }
        }
    }
    
    fun getCustomBlocks(): List<BuildingBlock> {
        return _uiState.value.buildingBlocks.filter { block ->
            // Custom blocks are those that are not in the default set
            !isDefaultBlock(block)
        }
    }
    
    private fun isDefaultBlock(block: BuildingBlock): Boolean {
        val defaultBlockNames = setOf(
            "Red Block", "Blue Block", "Green Block", "Yellow Block",
            "Purple Block", "Orange Block", "Cyan Block", "Pink Block"
        )
        return block.name in defaultBlockNames
    }
    
    fun addCustomBlock(name: String, color: Color) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            repository.addCustomBlock(name, color)
                .onSuccess {
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            showAddBlockDialog = false
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            errorMessage = error.message
                        )
                    }
                }
        }
    }
    
    fun deleteCustomBlock(blockId: String) {
        viewModelScope.launch {
            repository.deleteBlock(blockId)
                .onFailure { error ->
                    _uiState.update { 
                        it.copy(errorMessage = error.message)
                    }
                }
        }
    }
    
    fun setDragState(dragState: DragState) {
        _uiState.update { it.copy(dragState = dragState) }
    }
    
    fun setSelectionState(selectionState: SelectionState) {
        _uiState.update { it.copy(selectionState = selectionState) }
    }
    
    fun setZoomState(zoomState: ZoomState) {
        _uiState.update { it.copy(zoomState = zoomState) }
    }
    
    fun addDroppedBlock(block: DroppedBlock) {
        _uiState.update { state ->
            val newBlocks = state.droppedBlocks + block
            
            // Auto-connect to previous block if exists
            val newConnections = if (state.droppedBlocks.isNotEmpty()) {
                val previousBlock = state.droppedBlocks.maxByOrNull { it.order }
                if (previousBlock != null) {
                    val connection = createConnection(previousBlock, block)
                    state.connections + connection
                } else {
                    state.connections
                }
            } else {
                state.connections
            }
            
            state.copy(
                droppedBlocks = newBlocks,
                connections = newConnections,
                nextOrder = state.nextOrder + 1
            )
        }
    }
    
    fun updateDroppedBlockPosition(blockId: String, newPosition: Offset) {
        _uiState.update { state ->
            val updatedBlocks = state.droppedBlocks.map { block ->
                if (block.id == blockId) {
                    block.copy(position = newPosition)
                } else {
                    block
                }
            }
            
            // Update connections
            val updatedBlockCenter = Offset(
                newPosition.x + 40f,
                newPosition.y + 30f
            )
            
            val updatedConnections = state.connections.map { connection ->
                when {
                    connection.fromBlockId == blockId -> {
                        connection.copy(fromPosition = updatedBlockCenter)
                    }
                    connection.toBlockId == blockId -> {
                        connection.copy(toPosition = updatedBlockCenter)
                    }
                    else -> connection
                }
            }
            
            state.copy(
                droppedBlocks = updatedBlocks,
                connections = updatedConnections
            )
        }
    }
    
    fun removeDroppedBlock(blockId: String) {
        _uiState.update { state ->
            val filteredBlocks = state.droppedBlocks.filter { it.id != blockId }
            val filteredConnections = state.connections.filter { 
                it.fromBlockId != blockId && it.toBlockId != blockId 
            }
            
            state.copy(
                droppedBlocks = filteredBlocks,
                connections = filteredConnections,
                selectionState = if (state.selectionState.selectedBlockId == blockId) {
                    SelectionState()
                } else {
                    state.selectionState
                }
            )
        }
    }
    
    fun createManualConnection(fromBlockId: String, toBlockId: String) {
        _uiState.update { state ->
            val fromBlock = state.droppedBlocks.find { it.id == fromBlockId }
            val toBlock = state.droppedBlocks.find { it.id == toBlockId }
            
            if (fromBlock != null && toBlock != null) {
                // Check if connection already exists
                val connectionExists = state.connections.any { 
                    (it.fromBlockId == fromBlockId && it.toBlockId == toBlockId) ||
                    (it.fromBlockId == toBlockId && it.toBlockId == fromBlockId)
                }
                
                if (!connectionExists) {
                    val newConnection = createConnection(fromBlock, toBlock)
                    state.copy(connections = state.connections + newConnection)
                } else {
                    state
                }
            } else {
                state
            }
        }
    }
    
    fun clearAll() {
        _uiState.update { state ->
            state.copy(
                droppedBlocks = emptyList(),
                connections = emptyList(),
                nextOrder = 0,
                selectionState = SelectionState()
            )
        }
    }
    
    fun showAddBlockDialog() {
        _uiState.update { it.copy(showAddBlockDialog = true) }
    }
    
    fun hideAddBlockDialog() {
        _uiState.update { it.copy(showAddBlockDialog = false) }
    }
    
    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
    
    private fun createConnection(fromBlock: DroppedBlock, toBlock: DroppedBlock): Connection {
        val fromCenter = Offset(
            fromBlock.position.x + 40f,
            fromBlock.position.y + 30f
        )
        val toCenter = Offset(
            toBlock.position.x + 40f,
            toBlock.position.y + 30f
        )
        
        return Connection(
            fromBlockId = fromBlock.id,
            toBlockId = toBlock.id,
            fromPosition = fromCenter,
            toPosition = toCenter
        )
    }
}