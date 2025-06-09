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
    
    // Log memory info for debugging
    val runtime = Runtime.getRuntime()
    val maxMemory = runtime.maxMemory() / 1024 / 1024
    val totalMemory = runtime.totalMemory() / 1024 / 1024
    val freeMemory = runtime.freeMemory() / 1024 / 1024
    
    println("Starting DragMe server on $host:$port")
    println("Memory - Max: ${maxMemory}MB, Total: ${totalMemory}MB, Free: ${freeMemory}MB")
    
    try {
        embeddedServer(Netty, port = port, host = host, module = Application::module)
            .start(wait = true)
    } catch (e: Exception) {
        println("❌ Failed to start server: ${e.message}")
        e.printStackTrace()
    }
}

fun Application.module() {
    try {
        println("🔧 Configuring DragMe application module...")
        
        // Configure JSON serialization
        install(ContentNegotiation) {
            json(Json {
                prettyPrint = true
                isLenient = true
                ignoreUnknownKeys = true
            })
        }
        println("✅ JSON serialization configured")
        
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
            allowHeader(HttpHeaders.Accept)
            allowCredentials = false
            
            // Allow specific hosts
            allowHost("fauzisho.github.io", schemes = listOf("https"))
            allowHost("localhost:8081") // For local development  
            allowHost("localhost:8080") // For local development alternatives
            allowHost("localhost:3000") // For local development alternatives
            anyHost() // Allow all hosts for testing - you can restrict this later
        }
        println("✅ CORS configured")
        
        routing {
            // Health check endpoint - make it first priority
            get("/health") {
                call.respond(HttpStatusCode.OK, mapOf("status" to "OK", "service" to "DragMe API"))
            }
            
            // Root endpoint
            get("/") {
                call.respondText("DragMe API Server: ${Greeting().greet()}")
            }
            
            // API routes
            buildingBlockRoutes()
        }
        println("✅ Routing configured")
        
        // Log when server is ready
        environment.monitor.subscribe(ApplicationStarted) {
            println("🎉 DragMe Server is ready and healthy!")
            println("🔗 Health check: http://localhost:${environment.config.port}/health")
        }
        
        // Log when server stops
        environment.monitor.subscribe(ApplicationStopping) {
            println("🛑 DragMe Server is stopping...")
        }
        
    } catch (e: Exception) {
        println("❌ Error configuring application: ${e.message}")
        e.printStackTrace()
        throw e
    }
}
