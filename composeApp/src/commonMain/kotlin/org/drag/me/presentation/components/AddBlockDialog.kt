package org.drag.me.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun AddBlockDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, color: Color) -> Unit,
    isLoading: Boolean = false
) {
    var blockName by remember { mutableStateOf("") }
    var selectedColor by remember { mutableStateOf(Color.Red) }
    var showColorPicker by remember { mutableStateOf(false) }
    
    val predefinedColors = listOf(
        Color.Red,
        Color.Blue,
        Color.Green,
        Color.Yellow,
        Color.Magenta,
        Color.Cyan,
        Color(0xFFFF6B6B), // Light Red
        Color(0xFF4ECDC4), // Teal
        Color(0xFF45B7D1), // Sky Blue
        Color(0xFFF7DC6F), // Light Yellow
        Color(0xFFBB8FCE), // Light Purple
        Color(0xFF85C1E2), // Light Blue
        Color(0xFFF8B739), // Orange Yellow
        Color(0xFFEC7063), // Coral
        Color(0xFF52BE80), // Mint Green
        Color(0xFFAF7AC5)  // Purple
    )
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "🎨 Add Custom Block",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Block name input
                OutlinedTextField(
                    value = blockName,
                    onValueChange = { blockName = it },
                    label = { Text("Block Name") },
                    placeholder = { Text("My Custom Block") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                
                // Color selection
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Select Color",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF34495E)
                    )
                    
                    // Selected color preview
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(60.dp, 40.dp)
                                .background(
                                    color = selectedColor,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .border(
                                    width = 2.dp,
                                    color = Color(0xFFBDC3C7),
                                    shape = RoundedCornerShape(8.dp)
                                )
                        )
                        
                        Text(
                            text = "Preview: ${blockName.ifEmpty { "Custom Block" }}",
                            fontSize = 12.sp,
                            color = Color(0xFF7F8C8D)
                        )
                    }
                    
                    // Color grid
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(4),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.height(120.dp)
                    ) {
                        items(predefinedColors) { color ->
                            Box(
                                modifier = Modifier
                                    .aspectRatio(1f)
                                    .clip(CircleShape)
                                    .background(color)
                                    .border(
                                        width = if (color == selectedColor) 3.dp else 1.dp,
                                        color = if (color == selectedColor) {
                                            Color(0xFF2C3E50)
                                        } else {
                                            Color(0xFFBDC3C7)
                                        },
                                        shape = CircleShape
                                    )
                                    .clickable { selectedColor = color }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { 
                    if (blockName.isNotBlank()) {
                        onConfirm(blockName, selectedColor)
                    }
                },
                enabled = blockName.isNotBlank() && !isLoading,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF27AE60))
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Add Block", color = Color.White)
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isLoading
            ) {
                Text("Cancel")
            }
        }
    )
}