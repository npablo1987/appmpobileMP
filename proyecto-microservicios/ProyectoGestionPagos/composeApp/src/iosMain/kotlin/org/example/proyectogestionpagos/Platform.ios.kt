package org.example.proyectogestionpagos

import platform.UIKit.UIDevice

class IOSPlatform : Platform {
    override val name: String = UIDevice.currentDevice.systemName() + " " + UIDevice.currentDevice.systemVersion
}

actual fun getPlatform(): Platform = IOSPlatform()

actual fun getApiBaseUrl(): String = "http://localhost:8000"

actual fun getPaymentsBaseUrl(): String = "http://localhost:8002"
