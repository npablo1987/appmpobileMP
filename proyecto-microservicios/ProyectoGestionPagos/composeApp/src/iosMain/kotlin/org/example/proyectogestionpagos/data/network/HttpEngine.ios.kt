package org.example.proyectogestionpagos.data.network

import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.darwin.Darwin

actual fun provideHttpEngine(): HttpClientEngine = Darwin.create()
