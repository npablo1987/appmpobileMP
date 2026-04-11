package org.example.proyectogestionpagos.navigation

data class PaymentSuccessData(
    val idPago: Int,
    val mpPaymentId: Long? = null,
    val externalReference: String? = null,
    val monto: Double,
    val numeroFactura: String,
    val periodoMes: Int,
    val periodoAnio: Int,
)

sealed interface AppDestination {
    data object Login : AppDestination
    data object Home : AppDestination
    data object Profile : AppDestination
    data object InvoiceDetail : AppDestination
    data object Payment : AppDestination
    data object PaymentDirect : AppDestination
    data class PaymentSuccess(val data: PaymentSuccessData) : AppDestination
}
