package org.example.proyectogestionpagos.ui.screens

import kotlin.math.roundToInt
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.example.proyectogestionpagos.data.network.PaymentApiService
import org.example.proyectogestionpagos.data.model.PagoDirectoRequest
import org.example.proyectogestionpagos.data.session.SessionManager
import org.example.proyectogestionpagos.ui.theme.AppColors



private fun formatMoney(value: Double?): String {
    val amount = value ?: 0.0
    val rounded = (amount * 100.0).roundToInt() / 100.0
    return rounded.toString()
}

@Composable
fun PaymentDirectScreen(
    onBack: () -> Unit,
    onPaymentSuccess: () -> Unit,
) {
    val paymentApiService = remember { PaymentApiService() }
    val billingData = SessionManager.billingOverview
    val factura = billingData?.factura_actual

    var numeroTarjeta by remember { mutableStateOf("") }
    var nombreTitular by remember { mutableStateOf("APRO") }
    var mesVencimiento by remember { mutableStateOf("11") }
    var anioVencimiento by remember { mutableStateOf("2030") }
    var cvv by remember { mutableStateOf("123") }
    
    var isProcessing by remember { mutableStateOf(false) }
    var isWaitingPayment by remember { mutableStateOf(false) }
    var dialogMessage by remember { mutableStateOf<String?>(null) }
    var paymentStatus by remember { mutableStateOf<String?>(null) }
    var idPago by remember { mutableStateOf<Int?>(null) }
    var remainingSeconds by remember { mutableStateOf(0) }
    
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
            text = "Pago con Tarjeta",
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

        // Información de la factura
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = AppColors.Primary),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        "Factura:",
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        factura.numero_factura,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                    )
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        "Monto:",
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        "$ ${formatMoney(factura.total)}",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                    )
                }
            }
        }

        // Formulario
        if (!isWaitingPayment) {
            Text(
                text = "Datos de la Tarjeta",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = AppColors.PrimaryDark,
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = numeroTarjeta,
                onValueChange = { numeroTarjeta = it.take(19) },
                label = { Text("Número de tarjeta") },
                placeholder = { Text("4168 8188 4444 7115") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            )

            OutlinedTextField(
                value = nombreTitular,
                onValueChange = { nombreTitular = it },
                label = { Text("Nombre del titular") },
                placeholder = { Text("Ej: APRO") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                OutlinedTextField(
                    value = mesVencimiento,
                    onValueChange = { mesVencimiento = it.take(2) },
                    label = { Text("Mes") },
                    placeholder = { Text("11") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                )

                OutlinedTextField(
                    value = anioVencimiento,
                    onValueChange = { anioVencimiento = it.take(4) },
                    label = { Text("Año") },
                    placeholder = { Text("2030") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                )

                OutlinedTextField(
                    value = cvv,
                    onValueChange = { cvv = it.take(4) },
                    label = { Text("CVV") },
                    placeholder = { Text("123") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    visualTransformation = PasswordVisualTransformation(),
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        // Mostrar estado de espera con countdown
        if (isWaitingPayment) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF3E5F5)),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(60.dp),
                        color = AppColors.Primary,
                        strokeWidth = 4.dp
                    )

                    Text(
                        "Procesando pago...",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = AppColors.PrimaryDark,
                    )

                    Text(
                        "Escuchando respuesta de Mercado Pago",
                        fontSize = 12.sp,
                        color = Color.Gray,
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Countdown visual
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .background(
                                color = when {
                                    remainingSeconds > 60 -> Color(0xFFE8F5E9)
                                    remainingSeconds > 30 -> Color(0xFFFFF3E0)
                                    else -> Color(0xFFFDE1DE)
                                },
                                shape = RoundedCornerShape(50.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                "⏱️",
                                fontSize = 32.sp
                            )
                            Text(
                                "${remainingSeconds}s",
                                fontWeight = FontWeight.Bold,
                                fontSize = 24.sp,
                                color = when {
                                    remainingSeconds > 60 -> Color(0xFF2E7D32)
                                    remainingSeconds > 30 -> Color(0xFFF57C00)
                                    else -> Color(0xFF8C1D18)
                                }
                            )
                        }
                    }

                    // Progress bar
                    LinearProgressIndicator(
                        progress = remainingSeconds / 120f,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp),
                        color = when {
                            remainingSeconds > 60 -> Color(0xFF4CAF50)
                            remainingSeconds > 30 -> Color(0xFFFF9800)
                            else -> Color(0xFFF44336)
                        },
                        trackColor = Color.LightGray,
                    )

                    Text(
                        "Tiempo restante: ${remainingSeconds / 60}-${(remainingSeconds % 60).toString().padStart(2, '0')} minutos",
                        fontSize = 12.sp,
                        color = Color.Gray,
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        // Mostrar resultado del pago
        paymentStatus?.let { status ->
            val (bgColor, textColor, mensaje) = when (status) {
                "PAGADO" -> Triple(
                    Color(0xFFE8F5E9),
                    Color(0xFF2E7D32),
                    "✅ ¡Pago realizado exitosamente!\n\nTu pago ha sido procesado correctamente."
                )
                "RECHAZADO" -> Triple(
                    Color(0xFFFDE1DE),
                    Color(0xFF8C1D18),
                    "❌ Pago rechazado\n\nPor favor intenta con otra tarjeta"
                )
                "CANCELADO" -> Triple(
                    Color(0xFFFFF3E0),
                    Color(0xE65100),
                    "⏱️ Pago cancelado por tiempo\n\nEl tiempo de espera de 2 minutos se agotó. Por favor intenta nuevamente."
                )
                else -> Triple(
                    Color(0xFFF3E5F5),
                    Color(0xFF6A1B9A),
                    "Estado: $status"
                )
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = bgColor),
            ) {
                Text(
                    mensaje,
                    modifier = Modifier.padding(16.dp),
                    color = textColor,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Botón de pago
        if (!isWaitingPayment && paymentStatus == null) {
            Button(
                onClick = {
                    if (numeroTarjeta.isEmpty()) {
                        dialogMessage = "Ingresa el número de tarjeta"
                        return@Button
                    }
                    if (cvv.isEmpty()) {
                        dialogMessage = "Ingresa el CVV"
                        return@Button
                    }

                    val idUsuario = SessionManager.idUsuario
                    if (idUsuario == null) {
                        dialogMessage = "Debes iniciar sesión"
                        return@Button
                    }

                    isProcessing = true
                    paymentStatus = null
                    idPago = null

                    scope.launch {
                        try {
                            val pagoRequest = PagoDirectoRequest(
                                id_usuario = idUsuario,
                                numero_tarjeta = numeroTarjeta.replace(" ", "").replace("-", ""),
                                mes_vencimiento = mesVencimiento.toIntOrNull() ?: 11,
                                anio_vencimiento = anioVencimiento.toIntOrNull() ?: 2030,
                                cvv = cvv,
                                nombre_titular = nombreTitular,
                                email = "usuario@test.com",
                                descripcion = "Pago factura ${factura.numero_factura}",
                                monto = factura.total,
                            )

                            println("[PaymentDirectScreen] Enviando pago: $pagoRequest")
                            val response = paymentApiService.pagarDirecto(pagoRequest)
                            isProcessing = false

                            if (response.success && response.data != null) {
                                idPago = response.data.id_pago
                                remainingSeconds = 120
                                isWaitingPayment = true
                                println("[PaymentDirectScreen] Pago creado id_pago=${response.data.id_pago}")
                                
                                // Countdown timer - 2 minutos (120 segundos)
                                val maxWaitTime = 120
                                val pollInterval = 3_000L // 3 segundos entre polls
                                var elapsedSeconds = 0

                                while (elapsedSeconds < maxWaitTime && paymentStatus == null) {
                                    delay(pollInterval)
                                    elapsedSeconds += 3
                                    remainingSeconds = maxWaitTime - elapsedSeconds

                                    val statusResponse = paymentApiService.consultarEstado(response.data.id_pago)
                                    println("[PaymentDirectScreen] Estado: ${statusResponse?.estado} (${elapsedSeconds}s)")

                                    val estado = statusResponse?.estado?.uppercase() ?: ""
                                    when (estado) {
                                        "PAGADO" -> {
                                            paymentStatus = "PAGADO"
                                            isWaitingPayment = false
                                            delay(2000)
                                            onPaymentSuccess()
                                            return@launch
                                        }
                                        "RECHAZADO" -> {
                                            paymentStatus = "RECHAZADO"
                                            isWaitingPayment = false
                                            return@launch
                                        }
                                    }
                                }

                                // Timeout - se agotó el tiempo
                                if (paymentStatus == null) {
                                    paymentStatus = "CANCELADO"
                                    isWaitingPayment = false
                                }
                            } else {
                                isWaitingPayment = false
                                dialogMessage = response.message.ifEmpty { "Error al procesar el pago" }
                            }
                        } catch (e: Exception) {
                            isProcessing = false
                            isWaitingPayment = false
                            dialogMessage = "Error: ${e.message}"
                            println("[PaymentDirectScreen] Excepción: ${e.message}")
                            e.printStackTrace()
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = !isProcessing && !isWaitingPayment && paymentStatus == null,
                colors = ButtonDefaults.buttonColors(
                    containerColor = AppColors.Primary,
                    disabledContainerColor = Color.Gray
                ),
                shape = RoundedCornerShape(12.dp),
            ) {
                Text(
                    "PAGAR $ ${formatMoney(factura.total)}",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                )
            }
        }

        // Botones de acciones finales
        if (paymentStatus != null) {
            if (paymentStatus == "PAGADO") {
                Button(
                    onClick = { onPaymentSuccess() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AppColors.PrimaryDark
                    ),
                    shape = RoundedCornerShape(12.dp),
                ) {
                    Text(
                        "Continuar",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color.White,
                    )
                }
            } else {
                Button(
                    onClick = {
                        paymentStatus = null
                        numeroTarjeta = ""
                        nombreTitular = "APRO"
                        mesVencimiento = "11"
                        anioVencimiento = "2030"
                        cvv = "123"
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AppColors.Primary
                    ),
                    shape = RoundedCornerShape(12.dp),
                ) {
                    Text(
                        "Intentar nuevamente",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color.White,
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Información de tarjetas de prueba
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5)),
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    "🧪 Tarjetas de Prueba",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = AppColors.PrimaryDark,
                )
                Text(
                    "APRO - Pago aprobado",
                    fontSize = 11.sp,
                    color = Color.Gray,
                )
                Text(
                    "4168 8188 4444 7115",
                    fontSize = 10.sp,
                    color = Color.Gray,
                    fontFamily = FontFamily.Monospace,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "OTHE - Pago rechazado",
                    fontSize = 11.sp,
                    color = Color.Gray,
                )
                Text(
                    "5416 7526 0258 2580",
                    fontSize = 10.sp,
                    color = Color.Gray,
                    fontFamily = FontFamily.Monospace,
                )
            }
        }
    }

    // Diálogo de error
    dialogMessage?.let {
        AlertDialog(
            onDismissRequest = { dialogMessage = null },
            title = { Text("Error") },
            text = { Text(it) },
            confirmButton = {
                TextButton(onClick = { dialogMessage = null }) {
                    Text("OK")
                }
            },
        )
    }
}

data class Tuple4<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
