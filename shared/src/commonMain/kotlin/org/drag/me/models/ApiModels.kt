package org.drag.me.models

import kotlinx.serialization.Serializable

@Serializable
data class BuildingBlockDto(
    val id: String,
    val name: String,
    val colorHex: String
)

@Serializable
data class CreateBuildingBlockRequest(
    val name: String,
    val colorHex: String
)

@Serializable
data class BuildingBlocksResponse(
    val blocks: List<BuildingBlockDto>
)

@Serializable
data class ApiResponse<T>(
    val success: Boolean,
    val data: T? = null,
    val error: String? = null
)
