package org.drag.me.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BuildingBlockEntity(
    val id: String,
    val name: String,
    @SerialName("color_hex")
    val colorHex: String,
    @SerialName("is_default")
    val isDefault: Boolean = false,
    @SerialName("created_at")
    val createdAt: String? = null,
    @SerialName("updated_at")
    val updatedAt: String? = null
)

// Extension function to convert to DTO
fun BuildingBlockEntity.toDto(): BuildingBlockDto {
    return BuildingBlockDto(
        id = id,
        name = name,
        colorHex = colorHex
    )
}

// Extension function to convert from DTO
fun BuildingBlockDto.toEntity(isDefault: Boolean = false): BuildingBlockEntity {
    return BuildingBlockEntity(
        id = id,
        name = name,
        colorHex = colorHex,
        isDefault = isDefault
    )
}

@Serializable
data class CreateBuildingBlockEntity(
    val name: String,
    @SerialName("color_hex")
    val colorHex: String,
    @SerialName("is_default")
    val isDefault: Boolean = false
)

// Extension function to convert from request
fun CreateBuildingBlockRequest.toEntity(): CreateBuildingBlockEntity {
    return CreateBuildingBlockEntity(
        name = name,
        colorHex = colorHex,
        isDefault = false
    )
}
