package org.example.proyectogestionpagos.data.model

import kotlinx.serialization.Serializable

@Serializable
data class CrearPagoRequest(
    val id_usuario: Int,
    val descripcion: String,
    val monto: Double,
    val email_pagador: String = "usuario@email.cl",
)

@Serializable
data class CrearPagoData(
    val id_pago: Int,
    val url_pago: String,
    val external_reference: String,
)

@Serializable
data class CrearPagoResponse(
    val success: Boolean,
    val message: String,
    val data: CrearPagoData? = null,
)

@Serializable
data class EstadoPagoResponse(
    val id_pago: Int,
    val estado: String,
)

@Serializable
data class PagoDirectoRequest(
    val id_usuario: Int,
    val numero_tarjeta: String,
    val mes_vencimiento: Int,
    val anio_vencimiento: Int,
    val cvv: String,
    val nombre_titular: String,
    val email: String,
    val descripcion: String,
    val monto: Double,
)

@Serializable
data class PagoDirectoResponse(
    val success: Boolean,
    val message: String,
    val data: PagoDirectoData? = null,
)

@Serializable
data class PagoDirectoData(
    val id_pago: Int,
    val estado: String,
    val mp_payment_id: Long,
    val external_reference: String,
)
