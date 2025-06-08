package org.drag.me.data.api

import io.ktor.client.*

actual fun createHttpClient(): HttpClient {
    // Fallback to base client for iOS until dependencies are resolved
    return createHttpClientBase()
}
