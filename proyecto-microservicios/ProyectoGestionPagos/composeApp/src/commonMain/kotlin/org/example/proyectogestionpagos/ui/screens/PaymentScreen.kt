package org.example.proyectogestionpagos.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
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
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.example.proyectogestionpagos.data.network.PaymentApiService
import org.example.proyectogestionpagos.data.session.SessionManager
import org.example.proyectogestionpagos.ui.theme.AppColors
import org.example.proyectogestionpagos.utils.PaymentLauncher
import org.jetbrains.compose.resources.painterResource
import proyectogestionpagos.composeapp.generated.resources.Res
import proyectogestionpagos.composeapp.generated.resources.ic_home

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
        TextButton(onClick = onBack) { 
            Text("← Volver", color = AppColors.PrimaryDark)
        }

        Text(
            text = "Pago de factura",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = AppColors.PrimaryDark,
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (factura == null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFDE1DE)),
            ) {
                Text(
                    "No existe factura vigente para pagar",
                    modifier = Modifier.padding(16.dp),
                    color = Color(0xFF8C1D18),
                    fontWeight = FontWeight.SemiBold,
                )
            }
            return@Column
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = AppColors.Primary),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "Resumen de pago",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(modifier = Modifier.height(12.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Factura", color = Color.White.copy(alpha = 0.8f), style = MaterialTheme.typography.labelSmall)
                        Text(factura.numero_factura, color = Color.White, fontWeight = FontWeight.Bold)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Período", color = Color.White.copy(alpha = 0.8f), style = MaterialTheme.typography.labelSmall)
                        Text("${factura.periodo_mes}/${factura.periodo_anio}", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Total a pagar", color = Color.White.copy(alpha = 0.8f), style = MaterialTheme.typography.labelSmall)
                        Text(
                            text = "$${factura.total}",
                            style = MaterialTheme.typography.headlineSmall,
                            color = Color(0xFFFFB84D),
                            fontWeight = FontWeight.Bold,
                        )
                    }
                    StatusBadge(estadoPago)
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        if (isPreparingPayment || isCheckingStatus) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF2F3FA), RoundedCornerShape(12.dp))
                    .padding(16.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = AppColors.Primary,
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.size(12.dp))
                Text(
                    if (isPreparingPayment) "Preparando pago..." else "Consultando estado...",
                    color = AppColors.PrimaryDark,
                    fontWeight = FontWeight.SemiBold,
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(70.dp),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFEB5757)),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        ) {
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
                modifier = Modifier
                    .fillMaxSize(),
                enabled = !isPreparingPayment,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFEB5757),
                    disabledContainerColor = Color.Gray
                ),
                shape = RoundedCornerShape(14.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "💳",
                        fontSize = 24.sp,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Column(horizontalAlignment = Alignment.Start) {
                        Text(
                            "Pagar con",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp,
                            color = Color.White
                        )
                        Text(
                            "Mercado Pago",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Color.White
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

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
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            enabled = !isCheckingStatus && idPago != null,
            colors = ButtonDefaults.buttonColors(
                containerColor = AppColors.Primary,
                disabledContainerColor = Color.Gray
            ),
            shape = RoundedCornerShape(12.dp),
        ) {
            Text(
                "Consultar estado del pago",
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        val (statusBgColor, statusTextColor, statusIcon) = when (estadoPago) {
            "PAGADO" -> Triple(Color(0xFFDFF5E5), Color(0xFF19713E), "✓")
            "RECHAZADO", "CANCELADO" -> Triple(Color(0xFFFDE1DE), Color(0xFF8C1D18), "✕")
            else -> Triple(Color(0xFFFFF4D8), Color(0xFF7D5B00), "⏳")
        }
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = statusBgColor),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = statusIcon,
                    style = MaterialTheme.typography.headlineSmall,
                    color = statusTextColor,
                )
                Column {
                    Text(
                        text = "Estado del pago",
                        style = MaterialTheme.typography.labelSmall,
                        color = statusTextColor.copy(alpha = 0.8f),
                    )
                    Text(
                        text = estadoPago,
                        style = MaterialTheme.typography.titleSmall,
                        color = statusTextColor,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
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

@Composable
private fun StatusBadge(status: String) {
    val (bgColor, textColor) = when (status) {
        "PAGADO" -> Color(0xFF4CAF50) to Color.White
        "RECHAZADO", "CANCELADO" -> Color(0xFFE53935) to Color.White
        else -> Color(0xFFFFC107) to Color(0xFF7D5B00)
    }
    
    Text(
        text = status,
        modifier = Modifier
            .background(bgColor, RoundedCornerShape(8.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp),
        color = textColor,
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.Bold,
    )
}
