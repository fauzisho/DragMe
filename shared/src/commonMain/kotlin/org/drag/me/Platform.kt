package org.drag.me

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform