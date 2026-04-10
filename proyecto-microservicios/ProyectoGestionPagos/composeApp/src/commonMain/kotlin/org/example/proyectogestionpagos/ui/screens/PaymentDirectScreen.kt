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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import org.example.proyectogestionpagos.data.model.PagoDirectoRequest
import org.example.proyectogestionpagos.data.session.SessionManager
import org.example.proyectogestionpagos.ui.theme.AppColors
import org.example.proyectogestionpagos.ui.viewmodel.EstadoPagoUi
import org.example.proyectogestionpagos.ui.viewmodel.PaymentFlowViewModel
import kotlin.math.roundToInt

private fun formatMoney(value: Double?): String {
    val amount = value ?: 0.0
    val rounded = (amount * 100.0).roundToInt() / 100.0
    return rounded.toString()
}

private fun formatCountdown(segundosRestantes: Int): String {
    val minutos = (segundosRestantes / 60).toString().padStart(2, '0')
    val segundos = (segundosRestantes % 60).toString().padStart(2, '0')
    return "$minutos:$segundos"
}

@Composable
fun PaymentDirectScreen(
    onBack: () -> Unit,
    onPaymentSuccess: () -> Unit,
) {
    val billingData = SessionManager.billingOverview
    val factura = billingData?.factura_actual
    val viewModel = remember { PaymentFlowViewModel() }
    val uiState = viewModel.uiState

    var numeroTarjeta by remember { mutableStateOf("") }
    var nombreTitular by remember { mutableStateOf("APRO") }
    var mesVencimiento by remember { mutableStateOf("11") }
    var anioVencimiento by remember { mutableStateOf("2030") }
    var cvv by remember { mutableStateOf("123") }
    var dialogMessage by remember { mutableStateOf<String?>(null) }

    DisposableEffect(Unit) {
        onDispose {
            viewModel.cancelarPagoPorSalidaPantalla()
        }
    }

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
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Factura:", color = Color.White, fontWeight = FontWeight.SemiBold)
                    Text(factura.numero_factura, color = Color.White, fontWeight = FontWeight.Bold)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Monto:", color = Color.White, fontWeight = FontWeight.SemiBold)
                    Text(
                        "$ ${formatMoney(factura.total)}",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                    )
                }
            }
        }

        if (!uiState.esperandoConfirmacion) {
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
            )
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = mesVencimiento,
                    onValueChange = { mesVencimiento = it.take(2) },
                    label = { Text("Mes") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                )
                OutlinedTextField(
                    value = anioVencimiento,
                    onValueChange = { anioVencimiento = it.take(4) },
                    label = { Text("Año") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                )
                OutlinedTextField(
                    value = cvv,
                    onValueChange = { cvv = it.take(4) },
                    label = { Text("CVV") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    visualTransformation = PasswordVisualTransformation(),
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        if (uiState.esperandoConfirmacion) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF3E5F5)),
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    CircularProgressIndicator(color = AppColors.Primary, strokeWidth = 4.dp)
                    Text("Procesando pago", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = AppColors.PrimaryDark)
                    Text(
                        "Estamos esperando la confirmación de Mercado Pago",
                        color = Color.Gray,
                        textAlign = TextAlign.Center,
                        fontSize = 12.sp,
                    )

                    Text(
                        formatCountdown(uiState.segundosRestantes),
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 40.sp,
                        color = AppColors.PrimaryDark,
                        fontFamily = FontFamily.Monospace,
                    )

                    LinearProgressIndicator(
                        progress = { uiState.segundosRestantes / 120f },
                        modifier = Modifier.fillMaxWidth().height(8.dp),
                        color = AppColors.Primary,
                    )
                    Text(
                        "Tiempo restante para completar el pago: ${formatCountdown(uiState.segundosRestantes)}",
                        color = Color.Gray,
                        fontSize = 12.sp,
                    )

                    uiState.errorRedVisible?.let {
                        Text(text = it, color = Color(0xFFE65100), fontSize = 12.sp)
                    }

                    Button(
                        onClick = { viewModel.cancelarPagoPorUsuario() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB71C1C)),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                    ) {
                        Text("Cancelar pago", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        uiState.estadoFinal?.let {
            val mensaje = uiState.mensajeFinal ?: viewModel.textoEstadoAmigable(it)
            val (fondo, colorTexto) = when (it) {
                EstadoPagoUi.APROBADO -> Color(0xFFE8F5E9) to Color(0xFF2E7D32)
                EstadoPagoUi.RECHAZADO -> Color(0xFFFDE1DE) to Color(0xFF8C1D18)
                EstadoPagoUi.CANCELADO_USUARIO, EstadoPagoUi.CANCELADO, EstadoPagoUi.EXPIRADO -> Color(0xFFFFF3E0) to Color(0xFFE65100)
                else -> Color(0xFFF3E5F5) to Color(0xFF6A1B9A)
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = fondo),
            ) {
                Text(
                    mensaje,
                    modifier = Modifier.padding(16.dp),
                    color = colorTexto,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        if (!uiState.esperandoConfirmacion && uiState.estadoFinal == null) {
            Button(
                onClick = {
                    if (numeroTarjeta.isBlank()) {
                        dialogMessage = "Ingresa el número de tarjeta"
                        return@Button
                    }
                    if (cvv.isBlank()) {
                        dialogMessage = "Ingresa el CVV"
                        return@Button
                    }
                    val idUsuario = SessionManager.idUsuario
                    if (idUsuario == null) {
                        dialogMessage = "Debes iniciar sesión para pagar"
                        return@Button
                    }

                    val request = PagoDirectoRequest(
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

                    viewModel.iniciarFlujoPago(request) {
                        onPaymentSuccess()
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                enabled = !uiState.procesando,
                colors = ButtonDefaults.buttonColors(containerColor = AppColors.Primary),
                shape = RoundedCornerShape(12.dp),
            ) {
                Text("Pagar", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }

        if (uiState.estadoFinal != null) {
            Spacer(modifier = Modifier.height(8.dp))
            if (uiState.estadoFinal == EstadoPagoUi.APROBADO) {
                Button(
                    onClick = onPaymentSuccess,
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AppColors.PrimaryDark),
                    shape = RoundedCornerShape(12.dp),
                ) { Text("Aceptar", color = Color.White, fontWeight = FontWeight.Bold) }
            } else {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(
                        onClick = {
                            viewModel.limpiarEstadoFinal()
                            numeroTarjeta = ""
                        },
                        modifier = Modifier.weight(1f).height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AppColors.Primary),
                        shape = RoundedCornerShape(12.dp),
                    ) { Text("Reintentar", color = Color.White, fontWeight = FontWeight.Bold) }

                    Button(
                        onClick = onBack,
                        modifier = Modifier.weight(1f).height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AppColors.PrimaryDark),
                        shape = RoundedCornerShape(12.dp),
                    ) { Text("Volver", color = Color.White, fontWeight = FontWeight.Bold) }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5)),
        ) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Tarjetas de prueba Chile", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = AppColors.PrimaryDark)
                Text("Crédito Mastercard: 5416 7526 0258 2580 - CVV 123 - 11/30", fontSize = 10.sp, color = Color.Gray)
                Text("Crédito Visa: 4168 8188 4444 7115 - CVV 123 - 11/30", fontSize = 10.sp, color = Color.Gray)
                Text("Crédito American Express: 3757 781744 61804 - CVV 1234 - 11/30", fontSize = 10.sp, color = Color.Gray)
                Text("Débito Mastercard: 5241 0198 2664 6950 - CVV 123 - 11/30", fontSize = 10.sp, color = Color.Gray)
                Text("Débito Visa: 4023 6535 2391 4373 - CVV 123 - 11/30", fontSize = 10.sp, color = Color.Gray)
                Text("Titular APRO: aprobado | OTHE/CALL/FUND/SECU/EXPI/FORM: rechazado | CONT: pendiente", fontSize = 10.sp, color = Color.Gray)
            }
        }
    }

    dialogMessage?.let {
        AlertDialog(
            onDismissRequest = { dialogMessage = null },
            title = { Text("Aviso") },
            text = { Text(it) },
            confirmButton = {
                TextButton(onClick = { dialogMessage = null }) {
                    Text("Aceptar")
                }
            },
        )
    }
}
