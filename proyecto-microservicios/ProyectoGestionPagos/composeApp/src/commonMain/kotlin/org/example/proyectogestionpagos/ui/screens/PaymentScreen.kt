package org.example.proyectogestionpagos.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.example.proyectogestionpagos.data.network.PaymentApiService
import org.example.proyectogestionpagos.data.session.SessionManager
import org.example.proyectogestionpagos.ui.theme.AppColors
import org.example.proyectogestionpagos.utils.PaymentLauncher

@Composable
fun PaymentScreen(
    onBack: () -> Unit,
) {
    val paymentApiService = remember { PaymentApiService() }
    val billingData = SessionManager.billingOverview
    val factura = billingData?.factura_actual

    var isPreparingPayment by remember { mutableStateOf(false) }
    var isCheckingStatus by remember { mutableStateOf(false) }
    var dialogMessage by remember { mutableStateOf<String?>(null) }
    var idPago by remember { mutableStateOf<Int?>(null) }
    var estadoPago by remember { mutableStateOf("PENDIENTE") }
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.Start,
    ) {
        TextButton(onClick = onBack) { Text("← Volver") }

        Text(
            text = "Pago de factura",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = AppColors.PrimaryDark,
        )

        Spacer(modifier = Modifier.height(12.dp))

        if (factura == null) {
            Text("No existe factura vigente para pagar", color = Color(0xFFB3261E))
            return@Column
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text("Factura: ${factura.numero_factura}")
                Text("Total a pagar: $${factura.total}", fontWeight = FontWeight.Bold)
                Text("Estado actual: $estadoPago")
                Text("Período: ${factura.periodo_mes}/${factura.periodo_anio}")
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        if (isPreparingPayment || isCheckingStatus) {
            CircularProgressIndicator(color = AppColors.Primary)
            Spacer(modifier = Modifier.height(12.dp))
        }

        Button(
            onClick = {
                val idUsuario = SessionManager.idUsuario
                val monto = factura.total
                if (idUsuario == null) {
                    dialogMessage = "Debe iniciar sesión nuevamente"
                    return@Button
                }
                if (monto <= 0) {
                    dialogMessage = "Monto inválido para pago"
                    return@Button
                }

                println("[PaymentScreen] inicio de pago")
                isPreparingPayment = true
                scope.launch {
                    val response = paymentApiService.crearPago(
                        idUsuario = idUsuario,
                        descripcion = "Pago suscripción ${factura.numero_factura}",
                        monto = monto,
                    )
                    isPreparingPayment = false
                    if (!response.success || response.data == null) {
                        dialogMessage = response.message.ifEmpty { "No fue posible iniciar el pago" }
                        println("[PaymentScreen] error al crear pago: ${response.message}")
                        return@launch
                    }

                    idPago = response.data.id_pago
                    estadoPago = "PENDIENTE"
                    println("[PaymentScreen] redirección a Mercado Pago id_pago=${response.data.id_pago}")
                    PaymentLauncher.openPaymentUrl(response.data.url_pago)
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isPreparingPayment,
        ) {
            Text("Pagar ahora")
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = {
                val currentId = idPago
                if (currentId == null) {
                    dialogMessage = "Primero debes iniciar el pago"
                    return@Button
                }
                scope.launch {
                    isCheckingStatus = true
                    repeat(3) {
                        val statusResponse = paymentApiService.consultarEstado(currentId)
                        if (statusResponse != null) {
                            estadoPago = statusResponse.estado
                            println("[PaymentScreen] resultado estado pago=${statusResponse.estado}")
                            if (statusResponse.estado != "PENDIENTE") {
                                isCheckingStatus = false
                                return@launch
                            }
                        }
                        delay(1500)
                    }
                    isCheckingStatus = false
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isCheckingStatus,
        ) {
            Text("Consultar estado")
        }

        Spacer(modifier = Modifier.height(16.dp))

        val statusColor = when (estadoPago) {
            "PAGADO" -> Color(0xFF1B5E20)
            "RECHAZADO", "CANCELADO" -> Color(0xFFB71C1C)
            else -> Color(0xFF8A6D1F)
        }
        Text(
            text = "Estado del pago: $estadoPago",
            color = statusColor,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White, RoundedCornerShape(8.dp))
                .padding(12.dp),
        )
    }

    if (dialogMessage != null) {
        AlertDialog(
            onDismissRequest = { dialogMessage = null },
            confirmButton = {
                TextButton(onClick = { dialogMessage = null }) {
                    Text("Aceptar")
                }
            },
            title = { Text("Pago") },
            text = { Text(dialogMessage ?: "") },
        )
    }
}
