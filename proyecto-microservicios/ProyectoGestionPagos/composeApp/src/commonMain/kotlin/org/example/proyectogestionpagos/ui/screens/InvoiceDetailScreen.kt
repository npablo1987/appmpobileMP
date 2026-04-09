package org.example.proyectogestionpagos.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.example.proyectogestionpagos.data.session.SessionManager
import org.example.proyectogestionpagos.ui.theme.AppColors

@Composable
fun InvoiceDetailScreen(
    onBack: () -> Unit,
    onGoToPayment: () -> Unit,
) {
    val billingData = SessionManager.billingOverview
    val factura = billingData?.factura_actual

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
    ) {
        TextButton(onClick = onBack) {
            Text("← Volver")
        }

        Text(
            text = "Detalle de factura",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = AppColors.PrimaryDark,
        )

        Spacer(modifier = Modifier.height(12.dp))

        if (factura == null) {
            Text(
                text = "No se pudo obtener el detalle de la factura",
                color = Color(0xFFB3261E),
            )
            return@Column
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text("N° factura: ${factura.numero_factura}")
                Text("Fecha emisión: ${factura.fecha_emision}")
                Text("Estado factura: ${factura.estado_factura}")
                Text("Estado pago: ${factura.estado_pago}")
                Text("Período: ${factura.periodo_mes}/${factura.periodo_anio}")
                Spacer(modifier = Modifier.height(8.dp))
                Text("Subtotal: $${factura.subtotal}")
                Text("Impuesto: $${factura.impuesto}")
                Text(
                    text = "Total: $${factura.total}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.PrimaryDark,
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Ítems facturados",
            style = MaterialTheme.typography.titleMedium,
            color = AppColors.PrimaryDark,
            fontWeight = FontWeight.Bold,
        )
        Spacer(modifier = Modifier.height(8.dp))

        if (billingData.detalle_factura.isEmpty()) {
            Text("No hay ítems para mostrar", color = AppColors.NeutralText)
        } else {
            billingData.detalle_factura.forEach { item ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(item.descripcion_item, fontWeight = FontWeight.SemiBold)
                            Text("Tipo: ${item.tipo_item}", color = AppColors.NeutralText)
                            Text("Cantidad: ${item.cantidad}", color = AppColors.NeutralText)
                        }
                        Text("$${item.subtotal_item}", color = AppColors.PrimaryDark, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = {
                println("[InvoiceDetailScreen] Navegación a pago")
                onGoToPayment()
            },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Ir a pago")
        }
    }
}
