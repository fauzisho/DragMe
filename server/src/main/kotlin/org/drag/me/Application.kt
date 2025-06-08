package org.drag.me

import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.http.*
import kotlinx.serialization.json.Json
import org.drag.me.routes.buildingBlockRoutes

fun main() {
    val port = System.getenv("PORT")?.toInt() ?: SERVER_PORT
    val host = System.getenv("HOST") ?: "0.0.0.0"
    
    embeddedServer(Netty, port = port, host = host, module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    // Configure JSON serialization
    install(ContentNegotiation) {
        json(Json {
            prettyPrint = true
            isLenient = true
            ignoreUnknownKeys = true
        })
    }
    
    routing {
        // Configure CORS for GitHub Pages
        install(CORS) {
            allowMethod(HttpMethod.Options)
            allowMethod(HttpMethod.Get)
            allowMethod(HttpMethod.Post)
            allowMethod(HttpMethod.Put)
            allowMethod(HttpMethod.Delete)
            allowMethod(HttpMethod.Patch)
            allowHeader(HttpHeaders.Authorization)
            allowHeader(HttpHeaders.ContentType)
            allowCredentials = false
            
            // Allow your GitHub Pages domain
            allowHost("fauzisho.github.io", schemes = listOf("https"))
            allowHost("localhost:8081") // For local development
            allowHost("localhost:3000") // For local development alternatives
        }
        
        // Health check endpoint
        get("/") {
            call.respondText("DragMe API Server: ${Greeting().greet()}")
        }
        
        get("/health") {
            call.respondText("OK", ContentType.Text.Plain)
        }
        
        // API routes
        buildingBlockRoutes()
    }
}
