package org.example.proyectogestionpagos.data.model

import kotlinx.serialization.Serializable

@Serializable
data class GuardarTarjetaRequest(
    val id_usuario: Int,
    val token: String,
    val email: String,
)

@Serializable
data class TarjetaResponse(
    val id: Int,
    val id_usuario: Int,
    val mp_customer_id: String,
    val mp_card_id: String,
    val payment_method_id: String,
    val brand: String? = null,
    val last_four_digits: String,
    val expiration_month: Int,
    val expiration_year: Int,
    val holder_name: String,
    val is_default: Boolean,
    val created_at: String,
    val updated_at: String,
)

@Serializable
data class GuardarTarjetaResponse(
    val success: Boolean,
    val message: String,
    val data: TarjetaResponse? = null,
)

@Serializable
data class TarjetasListResponse(
    val success: Boolean,
    val message: String,
    val data: List<TarjetaResponse>,
)

@Serializable
data class TarjetaSetDefaultRequest(
    val is_default: Boolean,
)

@Serializable
data class TarjetaDeleteResponse(
    val success: Boolean,
    val message: String,
)

@Serializable
data class PagoConTarjetaGuardadaRequest(
    val id_usuario: Int,
    val id_tarjeta: Int,
    val descripcion: String,
    val monto: Double,
)

@Serializable
data class PagoConTarjetaGuardadaData(
    val id_pago: Int,
    val mp_payment_id: Long,
    val status: String,
    val status_detail: String? = null,
    val external_reference: String,
)

@Serializable
data class PagoConTarjetaGuardadaResponse(
    val success: Boolean,
    val message: String,
    val data: PagoConTarjetaGuardadaData? = null,
)
