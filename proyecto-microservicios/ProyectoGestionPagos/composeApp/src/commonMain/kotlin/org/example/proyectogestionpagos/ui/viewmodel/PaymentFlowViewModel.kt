package org.example.proyectogestionpagos.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.example.proyectogestionpagos.data.model.PagoDirectoRequest
import org.example.proyectogestionpagos.data.network.PaymentApiService

private const val SEGUNDOS_TIMEOUT = 120
private const val INTERVALO_POLL_MS = 3_000L
private const val MAX_ERRORES_RED_CONSECUTIVOS = 3

enum class EstadoPagoUi {
    APROBADO,
    PENDIENTE,
    EN_PROCESO,
    RECHAZADO,
    CANCELADO,
    CANCELADO_USUARIO,
    EXPIRADO,
    ERROR,
}

data class PaymentFlowUiState(
    val procesando: Boolean = false,
    val esperandoConfirmacion: Boolean = false,
    val idPago: Int? = null,
    val mpPaymentId: Long? = null,
    val externalReference: String? = null,
    val segundosRestantes: Int = SEGUNDOS_TIMEOUT,
    val estadoFinal: EstadoPagoUi? = null,
    val mensajeFinal: String? = null,
    val errorRedVisible: String? = null,
)

class PaymentFlowViewModel(
    private val paymentApiService: PaymentApiService = PaymentApiService(),
) {
    private val viewModelScope = CoroutineScope(Dispatchers.Main)
    private var pollingJob: Job? = null
    private var timerJob: Job? = null

    var uiState by mutableStateOf(PaymentFlowUiState())
        private set

    fun iniciarFlujoPago(request: PagoDirectoRequest, onPagoAprobado: () -> Unit) {
        limpiarEstadoFinal()
        uiState = uiState.copy(procesando = true, errorRedVisible = null)

        viewModelScope.launch {
            val respuesta = paymentApiService.pagarDirecto(request)
            if (!respuesta.success || respuesta.data == null) {
                uiState = uiState.copy(
                    procesando = false,
                    mensajeFinal = respuesta.message.ifEmpty { "No fue posible iniciar el pago" },
                    estadoFinal = EstadoPagoUi.ERROR,
                )
                return@launch
            }

            val idPagoCreado = respuesta.data.id_pago
            uiState = uiState.copy(
                procesando = false,
                esperandoConfirmacion = true,
                idPago = idPagoCreado,
                mpPaymentId = respuesta.data.mp_payment_id,
                externalReference = respuesta.data.external_reference,
                segundosRestantes = SEGUNDOS_TIMEOUT,
            )

            iniciarTemporizador()
            iniciarEscuchaEstado(idPagoCreado, onPagoAprobado)
        }
    }

    fun cancelarPagoPorUsuario() {
        val idPagoActual = uiState.idPago ?: return finalizarFlujo(EstadoPagoUi.CANCELADO_USUARIO)
        viewModelScope.launch {
            paymentApiService.cancelarPago(idPagoActual)
            finalizarFlujo(EstadoPagoUi.CANCELADO_USUARIO)
        }
    }

    fun cancelarPagoPorSalidaPantalla() {
        if (uiState.esperandoConfirmacion) {
            cancelarPagoPorUsuario()
        }
    }

    fun iniciarEscuchaWebPago(idPago: Int, onPagoAprobado: () -> Unit = {}) {
        limpiarEstadoFinal()
        uiState = uiState.copy(
            esperandoConfirmacion = true,
            idPago = idPago,
            segundosRestantes = SEGUNDOS_TIMEOUT,
            procesando = false,
        )
        iniciarTemporizador()
        iniciarEscuchaEstado(idPago, onPagoAprobado)
    }

    fun limpiarEstadoFinal() {
        uiState = uiState.copy(estadoFinal = null, mensajeFinal = null, errorRedVisible = null)
    }

    private fun iniciarTemporizador() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (uiState.segundosRestantes > 0 && uiState.esperandoConfirmacion) {
                delay(1_000)
                uiState = uiState.copy(segundosRestantes = uiState.segundosRestantes - 1)
            }
            if (uiState.esperandoConfirmacion && uiState.segundosRestantes <= 0) {
                val idPagoActual = uiState.idPago
                if (idPagoActual != null) {
                    paymentApiService.cancelarPago(idPagoActual)
                }
                finalizarFlujo(EstadoPagoUi.EXPIRADO)
            }
        }
    }

    private fun iniciarEscuchaEstado(idPago: Int, onPagoAprobado: () -> Unit) {
        pollingJob?.cancel()
        pollingJob = viewModelScope.launch {
            var erroresConsecutivos = 0

            while (uiState.esperandoConfirmacion) {
                delay(INTERVALO_POLL_MS)
                val estadoResponse = paymentApiService.consultarEstado(idPago)

                if (estadoResponse == null) {
                    erroresConsecutivos += 1
                    uiState = uiState.copy(errorRedVisible = "Problemas de red. Reintentando...")
                    if (erroresConsecutivos >= MAX_ERRORES_RED_CONSECUTIVOS) {
                        finalizarFlujo(EstadoPagoUi.ERROR, "Error de red al consultar estado del pago")
                    }
                    continue
                }

                erroresConsecutivos = 0
                uiState = uiState.copy(errorRedVisible = null)

                when (mapearEstadoBackend(estadoResponse.estado)) {
                    EstadoPagoUi.APROBADO -> {
                        finalizarFlujo(EstadoPagoUi.APROBADO)
                        onPagoAprobado()
                    }
                    EstadoPagoUi.RECHAZADO -> finalizarFlujo(EstadoPagoUi.RECHAZADO)
                    EstadoPagoUi.CANCELADO -> finalizarFlujo(EstadoPagoUi.CANCELADO)
                    EstadoPagoUi.EXPIRADO -> finalizarFlujo(EstadoPagoUi.EXPIRADO)
                    EstadoPagoUi.PENDIENTE,
                    EstadoPagoUi.EN_PROCESO,
                    EstadoPagoUi.CANCELADO_USUARIO,
                    EstadoPagoUi.ERROR -> Unit
                }
            }
        }
    }

    private fun finalizarFlujo(estado: EstadoPagoUi, mensajePersonalizado: String? = null) {
        pollingJob?.cancel()
        timerJob?.cancel()
        uiState = uiState.copy(
            esperandoConfirmacion = false,
            procesando = false,
            estadoFinal = estado,
            mensajeFinal = mensajePersonalizado ?: mensajePorEstado(estado),
        )
    }

    private fun mapearEstadoBackend(estado: String): EstadoPagoUi {
        return when (estado.uppercase()) {
            "PAGADO", "APPROVED" -> EstadoPagoUi.APROBADO
            "PENDIENTE", "PENDING" -> EstadoPagoUi.PENDIENTE
            "EN_PROCESO", "IN_PROCESS", "IN PROCESS" -> EstadoPagoUi.EN_PROCESO
            "RECHAZADO", "REJECTED" -> EstadoPagoUi.RECHAZADO
            "CANCELADO", "CANCELLED", "ANULADO" -> EstadoPagoUi.CANCELADO
            "EXPIRADO", "EXPIRED", "TIMEOUT" -> EstadoPagoUi.EXPIRADO
            else -> EstadoPagoUi.PENDIENTE
        }
    }

    fun textoEstadoAmigable(estado: EstadoPagoUi?): String {
        return when (estado) {
            EstadoPagoUi.APROBADO -> "Pago aprobado"
            EstadoPagoUi.PENDIENTE -> "Pago pendiente"
            EstadoPagoUi.EN_PROCESO -> "Pago en proceso"
            EstadoPagoUi.RECHAZADO -> "Pago rechazado"
            EstadoPagoUi.CANCELADO -> "Pago cancelado"
            EstadoPagoUi.CANCELADO_USUARIO -> "Pago cancelado por el usuario"
            EstadoPagoUi.EXPIRADO -> "La compra fue cancelada por tiempo de espera"
            EstadoPagoUi.ERROR -> "No fue posible procesar el pago"
            null -> ""
        }
    }

    private fun mensajePorEstado(estado: EstadoPagoUi): String {
        return when (estado) {
            EstadoPagoUi.APROBADO -> "Pago aprobado"
            EstadoPagoUi.RECHAZADO -> "Pago rechazado"
            EstadoPagoUi.CANCELADO_USUARIO -> "Pago cancelado por el usuario"
            EstadoPagoUi.CANCELADO -> "Pago cancelado"
            EstadoPagoUi.EXPIRADO -> "La compra fue cancelada por tiempo de espera"
            EstadoPagoUi.ERROR -> "Ocurrió un problema al procesar el pago"
            EstadoPagoUi.PENDIENTE -> "Pago pendiente"
            EstadoPagoUi.EN_PROCESO -> "Pago en proceso"
        }
    }
}
