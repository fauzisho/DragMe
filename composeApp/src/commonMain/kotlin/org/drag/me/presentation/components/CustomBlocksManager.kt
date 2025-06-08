package org.drag.me.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.drag.me.model.BuildingBlock

@Composable
fun CustomBlocksManager(
    customBlocks: List<BuildingBlock>,
    onAddClick: () -> Unit,
    onDeleteBlock: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var blockToDelete by remember { mutableStateOf<BuildingBlock?>(null) }
    
    Card(
        modifier = modifier.fillMaxWidth(),
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
                    text = "🎨 Custom Blocks",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF34495E)
                )
                
                // Add button
                FilledIconButton(
                    onClick = onAddClick,
                    modifier = Modifier.size(36.dp),
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = Color(0xFF27AE60)
                    )
                ) {
                    Text(
                        text = "+",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            if (customBlocks.isEmpty()) {
                Text(
                    text = "No custom blocks yet. Click + to add one!",
                    fontSize = 14.sp,
                    color = Color(0xFF95A5A6),
                    modifier = Modifier.padding(vertical = 16.dp)
                )
            } else {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(customBlocks) { block ->
                        CustomBlockItem(
                            block = block,
                            onDeleteClick = { blockToDelete = block }
                        )
                    }
                }
            }
        }
    }
    
    // Delete confirmation dialog
    blockToDelete?.let { block ->
        AlertDialog(
            onDismissRequest = { blockToDelete = null },
            title = {
                Text(
                    text = "Delete Custom Block",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to delete '${block.name}'?",
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteBlock(block.id)
                        blockToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE74C3C))
                ) {
                    Text("Delete", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { blockToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun CustomBlockItem(
    block: BuildingBlock,
    onDeleteClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(80.dp, 80.dp)
    ) {
        // Block
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .shadow(
                    elevation = 2.dp,
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
        
        // Delete button
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = 8.dp, y = (-8).dp)
                .size(24.dp)
                .background(
                    color = Color(0xFFE74C3C),
                    shape = CircleShape
                )
                .clickable { onDeleteClick() },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "×",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}