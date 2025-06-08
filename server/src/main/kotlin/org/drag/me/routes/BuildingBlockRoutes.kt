package org.drag.me.routes

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.drag.me.models.ApiResponse
import org.drag.me.models.BuildingBlocksResponse
import org.drag.me.models.CreateBuildingBlockRequest
import org.drag.me.service.BuildingBlockService

fun Route.buildingBlockRoutes() {
    val service = BuildingBlockService()
    
    route("/api/building-blocks") {
        
        // GET /api/building-blocks - Get all building blocks
        get {
            try {
                val blocks = service.getAllBlocks()
                val response = ApiResponse(
                    success = true,
                    data = BuildingBlocksResponse(blocks)
                )
                call.respond(HttpStatusCode.OK, response)
            } catch (e: Exception) {
                val errorResponse = ApiResponse<BuildingBlocksResponse>(
                    success = false,
                    error = "Failed to fetch building blocks: ${e.message}"
                )
                call.respond(HttpStatusCode.InternalServerError, errorResponse)
            }
        }
        
        // POST /api/building-blocks - Create a new custom building block
        post {
            try {
                val request = call.receive<CreateBuildingBlockRequest>()
                
                // Validate request
                if (request.name.isBlank()) {
                    val errorResponse = ApiResponse<Nothing>(
                        success = false,
                        error = "Block name cannot be empty"
                    )
                    call.respond(HttpStatusCode.BadRequest, errorResponse)
                    return@post
                }
                
                // Validate hex color format
                if (!request.colorHex.matches(Regex("^#[0-9A-Fa-f]{6}$"))) {
                    val errorResponse = ApiResponse<Nothing>(
                        success = false,
                        error = "Invalid color format. Use hex format like #FF0000"
                    )
                    call.respond(HttpStatusCode.BadRequest, errorResponse)
                    return@post
                }
                
                val block = service.createBlock(request)
                val response = ApiResponse(
                    success = true,
                    data = block
                )
                call.respond(HttpStatusCode.Created, response)
                
            } catch (e: Exception) {
                val errorResponse = ApiResponse<Nothing>(
                    success = false,
                    error = "Failed to create building block: ${e.message}"
                )
                call.respond(HttpStatusCode.InternalServerError, errorResponse)
            }
        }
        
        // DELETE /api/building-blocks/{id} - Delete a building block
        delete("/{id}") {
            try {
                val id = call.parameters["id"]
                if (id.isNullOrBlank()) {
                    val errorResponse = ApiResponse<Nothing>(
                        success = false,
                        error = "Block ID is required"
                    )
                    call.respond(HttpStatusCode.BadRequest, errorResponse)
                    return@delete
                }
                
                val deleted = service.deleteBlock(id)
                if (deleted) {
                    val response = ApiResponse(
                        success = true,
                        data = "Block deleted successfully"
                    )
                    call.respond(HttpStatusCode.OK, response)
                } else {
                    val errorResponse = ApiResponse<Nothing>(
                        success = false,
                        error = "Block not found or cannot be deleted (default blocks cannot be deleted)"
                    )
                    call.respond(HttpStatusCode.NotFound, errorResponse)
                }
                
            } catch (e: Exception) {
                val errorResponse = ApiResponse<Nothing>(
                    success = false,
                    error = "Failed to delete building block: ${e.message}"
                )
                call.respond(HttpStatusCode.InternalServerError, errorResponse)
            }
        }
        
        // GET /api/building-blocks/{id} - Get a specific building block
        get("/{id}") {
            try {
                val id = call.parameters["id"]
                if (id.isNullOrBlank()) {
                    val errorResponse = ApiResponse<Nothing>(
                        success = false,
                        error = "Block ID is required"
                    )
                    call.respond(HttpStatusCode.BadRequest, errorResponse)
                    return@get
                }
                
                val block = service.getBlockById(id)
                if (block != null) {
                    val response = ApiResponse(
                        success = true,
                        data = block
                    )
                    call.respond(HttpStatusCode.OK, response)
                } else {
                    val errorResponse = ApiResponse<Nothing>(
                        success = false,
                        error = "Block not found"
                    )
                    call.respond(HttpStatusCode.NotFound, errorResponse)
                }
                
            } catch (e: Exception) {
                val errorResponse = ApiResponse<Nothing>(
                    success = false,
                    error = "Failed to fetch building block: ${e.message}"
                )
                call.respond(HttpStatusCode.InternalServerError, errorResponse)
            }
        }
    }
}
