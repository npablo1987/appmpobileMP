package org.example.proyectogestionpagos

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform
expect fun getApiBaseUrl(): String

expect fun getPaymentsBaseUrl(): String
