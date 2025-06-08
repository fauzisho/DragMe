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
    val host = "0.0.0.0" // Important: bind to all interfaces, not localhost
    
    println("Starting server on $host:$port")
    
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
        
        // Health check endpoint - make it first priority
        get("/health") {
            call.respond(HttpStatusCode.OK, "OK")
        }
        
        // Root endpoint
        get("/") {
            call.respondText("DragMe API Server: ${Greeting().greet()}")
        }
        
        // API routes
        buildingBlockRoutes()
    }
    
    // Log when server is ready
    environment.monitor.subscribe(ApplicationStarted) {
        println("✅ DragMe Server is ready and healthy!")
    }
}
