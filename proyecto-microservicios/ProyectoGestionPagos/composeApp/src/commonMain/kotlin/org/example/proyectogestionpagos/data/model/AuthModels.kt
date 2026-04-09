package org.example.proyectogestionpagos.data.model

import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest(
    val correo: String,
    val clave: String,
)

@Serializable
data class LoginUserData(
    val id_usuario: Int,
    val rut: String? = null,
    val nombres: String,
    val apellido_paterno: String,
    val apellido_materno: String? = null,
    val correo: String,
    val telefono: String? = null,
    val ciudad: String? = null,
    val estado_cuenta: String,
)

@Serializable
data class LoginResponse(
    val success: Boolean,
    val message: String,
    val data: LoginUserData? = null,
)

@Serializable
data class HomeUserData(
    val id_usuario: Int,
    val rut: String? = null,
    val nombre_completo: String,
    val correo: String,
    val telefono: String? = null,
    val direccion: String? = null,
    val ciudad: String? = null,
    val estado_cuenta: String,
    val fecha_registro: String,
)

@Serializable
data class HomeResponse(
    val success: Boolean,
    val message: String,
    val data: HomeUserData? = null,
)

@Serializable
data class BillingOverviewResponse(
    val success: Boolean,
    val message: String,
    val data: BillingOverviewData? = null,
)

@Serializable
data class BillingOverviewData(
    val id_usuario: Int,
    val estado_cuenta: String,
    val suscripciones: List<SuscripcionData> = emptyList(),
    val servicios_adicionales: List<UsuarioServicioData> = emptyList(),
    val factura_actual: FacturaActualData? = null,
    val detalle_factura: List<DetalleFacturaData> = emptyList(),
)

@Serializable
data class SuscripcionData(
    val id_suscripcion: Int,
    val id_plan: Int,
    val fecha_inicio: String,
    val fecha_fin: String? = null,
    val estado_suscripcion: String,
    val renovacion_automatica: Boolean,
    val nombre_plan: String,
    val descripcion: String? = null,
    val precio_mensual: Double,
    val limite_usuarios: Int,
)

@Serializable
data class UsuarioServicioData(
    val id_usuario_servicio: Int,
    val id_servicio: Int,
    val fecha_contratacion: String,
    val fecha_termino: String? = null,
    val estado: String,
    val precio_pactado: Double,
    val nombre_servicio: String,
    val descripcion: String? = null,
    val costo_mensual: Double,
)

@Serializable
data class FacturaActualData(
    val id_factura: Int,
    val numero_factura: String,
    val fecha_emision: String,
    val subtotal: Double,
    val impuesto: Double,
    val total: Double,
    val estado_factura: String,
    val id_pago: Int,
    val periodo_anio: Int,
    val periodo_mes: Int,
    val estado_pago: String,
)

@Serializable
data class DetalleFacturaData(
    val id_detalle_factura: Int,
    val id_factura: Int,
    val tipo_item: String,
    val descripcion_item: String,
    val cantidad: Int,
    val precio_unitario: Double,
    val subtotal_item: Double,
)

@Serializable
data class PreparePaymentRequest(
    val id_factura: Int,
)

@Serializable
data class PreparePaymentResponse(
    val success: Boolean,
    val message: String,
)
