package org.example.proyectogestionpagos.ui.screens

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
import kotlinx.coroutines.launch
import org.example.proyectogestionpagos.data.network.AuthApiService
import org.example.proyectogestionpagos.data.session.SessionManager
import org.example.proyectogestionpagos.ui.theme.AppColors

@Composable
fun PaymentScreen(
    onBack: () -> Unit,
) {
    val authApiService = remember { AuthApiService() }
    val billingData = SessionManager.billingOverview
    val factura = billingData?.factura_actual

    var isPreparingPayment by remember { mutableStateOf(false) }
    var dialogMessage by remember { mutableStateOf<String?>(null) }
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
                Text("Estado: ${factura.estado_factura}")
                Text("Período: ${factura.periodo_mes}/${factura.periodo_anio}")
                Spacer(modifier = Modifier.height(8.dp))
                Text("Incluye plan y servicios contratados por el usuario.")
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        if (isPreparingPayment) {
            CircularProgressIndicator(color = AppColors.Primary)
        }

        Button(
            onClick = {
                val idUsuario = SessionManager.idUsuario
                if (idUsuario == null) {
                    dialogMessage = "Debe iniciar sesión nuevamente"
                    return@Button
                }

                println("[PaymentScreen] Inicio de preparación de pago")
                isPreparingPayment = true
                scope.launch {
                    val response = authApiService.preparePayment(idUsuario, factura.id_factura)
                    isPreparingPayment = false
                    dialogMessage = if (response.success) {
                        "Pago preparado correctamente"
                    } else {
                        response.message.ifEmpty { "No fue posible preparar el pago" }
                    }
                    println("[PaymentScreen] Resultado preparación pago: ${response.message}")
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isPreparingPayment,
        ) {
            Text("Confirmar pago")
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
