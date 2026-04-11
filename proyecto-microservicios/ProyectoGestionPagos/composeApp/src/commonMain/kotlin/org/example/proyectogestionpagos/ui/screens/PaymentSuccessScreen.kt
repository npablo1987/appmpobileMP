package org.example.proyectogestionpagos.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.example.proyectogestionpagos.navigation.PaymentSuccessData
import org.example.proyectogestionpagos.ui.theme.AppColors

private val GreenDark = Color(0xFF1B5E20)
private val GreenMid = Color(0xFF2E7D32)
private val GreenLight = Color(0xFF43A047)
private val GreenSurface = Color(0xFFE8F5E9)
private val GreenAccent = Color(0xFF66BB6A)

private fun nombreMes(mes: Int): String = when (mes) {
    1 -> "Enero"; 2 -> "Febrero"; 3 -> "Marzo"; 4 -> "Abril"
    5 -> "Mayo"; 6 -> "Junio"; 7 -> "Julio"; 8 -> "Agosto"
    9 -> "Septiembre"; 10 -> "Octubre"; 11 -> "Noviembre"; 12 -> "Diciembre"
    else -> "Mes $mes"
}

private fun formatMonto(monto: Double): String {
    val entero = monto.toLong()
    val formatted = buildString {
        val str = entero.toString()
        var count = 0
        for (i in str.indices.reversed()) {
            if (count > 0 && count % 3 == 0) insert(0, '.')
            insert(0, str[i])
            count++
        }
    }
    return "$ $formatted"
}

@Composable
fun PaymentSuccessScreen(
    data: PaymentSuccessData,
    onGoHome: () -> Unit,
) {
    val scale = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        scale.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing),
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Header con gradiente verde
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(GreenMid, GreenDark),
                    ),
                )
                .padding(top = 40.dp, bottom = 36.dp),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                // Ícono de check animado
                Box(
                    modifier = Modifier
                        .scale(scale.value)
                        .size(88.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(Color.White),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "✓",
                            fontSize = 36.sp,
                            fontWeight = FontWeight.Bold,
                            color = GreenMid,
                        )
                    }
                }

                Text(
                    text = "¡Pago Aprobado!",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                )

                Text(
                    text = "Tu pago fue procesado con éxito",
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.85f),
                    textAlign = TextAlign.Center,
                )

                // Monto destacado
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White.copy(alpha = 0.18f))
                        .padding(horizontal = 28.dp, vertical = 12.dp),
                ) {
                    Text(
                        text = formatMonto(data.monto),
                        fontSize = 32.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                    )
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(top = 20.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            // Badge de estado
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(GreenSurface)
                        .padding(horizontal = 20.dp, vertical = 8.dp),
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(GreenAccent),
                        )
                        Text(
                            text = "PAGO ACREDITADO",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = GreenMid,
                            letterSpacing = 1.sp,
                        )
                    }
                }
            }

            // Tarjeta: Detalle de la factura
            SectionCard(title = "Detalle de la Factura") {
                DetailRow(label = "Número de factura", value = data.numeroFactura)
                DetailDivider()
                DetailRow(label = "Período", value = "${nombreMes(data.periodoMes)} ${data.periodoAnio}")
                DetailDivider()
                DetailRow(label = "Monto pagado", value = formatMonto(data.monto), valueColor = GreenMid, valueBold = true)
            }

            // Tarjeta: Comprobante de transacción
            SectionCard(title = "Comprobante de Transacción") {
                DetailRow(label = "N° de operación", value = "#${data.idPago}")
                if (data.mpPaymentId != null) {
                    DetailDivider()
                    DetailRow(label = "ID Mercado Pago", value = data.mpPaymentId.toString())
                }
                if (!data.externalReference.isNullOrBlank()) {
                    DetailDivider()
                    DetailRow(
                        label = "Referencia",
                        value = data.externalReference,
                        valueSize = 11.sp,
                        maxLines = 2,
                    )
                }
                DetailDivider()
                DetailRow(label = "Método de pago", value = "Tarjeta · Mercado Pago")
                DetailDivider()
                DetailRow(label = "Estado", value = "Aprobado", valueColor = GreenMid, valueBold = true)
            }

            // Aviso informativo
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F7FF)),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.Top,
                ) {
                    Text("ℹ️", fontSize = 16.sp)
                    Text(
                        text = "Guarda tu número de operación como comprobante. " +
                            "Puedes ver el historial de pagos en tu perfil.",
                        fontSize = 12.sp,
                        color = Color(0xFF1565C0),
                        lineHeight = 18.sp,
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Botón volver al inicio
            Button(
                onClick = onGoHome,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = GreenMid),
                shape = RoundedCornerShape(14.dp),
            ) {
                Text(
                    "Volver al inicio",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color.White,
                )
            }
        }
    }
}

@Composable
private fun SectionCard(
    title: String,
    content: @Composable () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = AppColors.PrimaryDark,
                letterSpacing = 0.5.sp,
            )
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
private fun DetailRow(
    label: String,
    value: String,
    valueColor: Color = Color(0xFF1A1A2E),
    valueBold: Boolean = false,
    valueSize: androidx.compose.ui.unit.TextUnit = 14.sp,
    maxLines: Int = 1,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top,
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            color = Color(0xFF757575),
            modifier = Modifier.weight(1f),
        )
        Text(
            text = value,
            fontSize = valueSize,
            color = valueColor,
            fontWeight = if (valueBold) FontWeight.Bold else FontWeight.Medium,
            textAlign = TextAlign.End,
            maxLines = maxLines,
            modifier = Modifier
                .weight(1f)
                .padding(start = 8.dp),
        )
    }
}

@Composable
private fun DetailDivider() {
    Divider(
        modifier = Modifier.padding(vertical = 2.dp),
        color = Color(0xFFF0F0F0),
        thickness = 1.dp,
    )
}
