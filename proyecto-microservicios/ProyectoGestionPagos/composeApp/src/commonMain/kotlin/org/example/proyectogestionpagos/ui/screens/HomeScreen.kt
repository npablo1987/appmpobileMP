package org.example.proyectogestionpagos.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.example.proyectogestionpagos.data.model.BillingOverviewData
import org.example.proyectogestionpagos.data.model.SuscripcionData
import org.example.proyectogestionpagos.data.model.UsuarioServicioData
import org.example.proyectogestionpagos.data.network.AuthApiService
import org.example.proyectogestionpagos.data.session.SessionManager
import org.example.proyectogestionpagos.ui.components.AppBottomBar
import org.example.proyectogestionpagos.ui.theme.AppColors
import org.jetbrains.compose.resources.painterResource
import proyectogestionpagos.composeapp.generated.resources.Res
import proyectogestionpagos.composeapp.generated.resources.ic_home
import proyectogestionpagos.composeapp.generated.resources.ic_logout

@Composable
fun HomeScreen(
    onLogout: () -> Unit,
    onProfileClick: () -> Unit,
    onOpenInvoiceDetail: () -> Unit,
    onGoToPayment: () -> Unit,
    onBack: () -> Unit = {},
) {
    val authApiService = remember { AuthApiService() }
    var isLoading by remember { mutableStateOf(true) }
    var billingData by remember { mutableStateOf<BillingOverviewData?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        println("[HomeScreen] Inicio de carga de suscripciones")
        val idUsuario = SessionManager.idUsuario
        if (idUsuario == null) {
            println("[HomeScreen] Sesión no válida")
            errorMessage = "Debe iniciar sesión nuevamente"
            isLoading = false
            return@LaunchedEffect
        }

        val response = authApiService.getBillingOverview(idUsuario)
        if (response.success && response.data != null) {
            println("[HomeScreen] Datos de suscripciones cargados exitosamente")
            billingData = response.data
            SessionManager.saveBillingOverview(response.data)
        } else {
            println("[HomeScreen] Error carga resumen: ${response.message}")
            errorMessage = response.message
        }
        isLoading = false
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                painter = painterResource(Res.drawable.ic_home),
                contentDescription = "Inicio",
                tint = AppColors.PrimaryDark,
            )
            Text(
                text = "Suscripciones y facturación",
                modifier = Modifier.padding(start = 8.dp),
                style = MaterialTheme.typography.titleLarge,
                color = AppColors.PrimaryDark,
                fontWeight = FontWeight.Bold,
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        when {
            isLoading -> {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                ) {
                    Row(
                        modifier = Modifier.padding(18.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        CircularProgressIndicator(color = AppColors.Primary)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Cargando suscripciones y facturación...")
                    }
                }
            }

            billingData != null -> {
                BillingResumeCard(
                    data = billingData!!,
                    onOpenInvoiceDetail = onOpenInvoiceDetail,
                    onGoToPayment = onGoToPayment,
                )

                Spacer(modifier = Modifier.height(12.dp))
                SuscripcionesSection(billingData!!.suscripciones)

                Spacer(modifier = Modifier.height(12.dp))
                ServiciosSection(billingData!!.servicios_adicionales)

                Spacer(modifier = Modifier.height(12.dp))
                AppBottomBar(
                    currentSection = "home",
                    onHomeClick = {},
                    onProfileClick = onProfileClick,
                )
            }

            else -> {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                ) {
                    Text(
                        text = "Error al cargar la información. Inténtalo nuevamente.",
                        modifier = Modifier.padding(16.dp),
                        color = Color(0xFFB3261E),
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
        Button(
            onClick = {
                SessionManager.clearSession()
                onLogout()
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(containerColor = AppColors.PrimaryDark),
        ) {
            Icon(
                painter = painterResource(Res.drawable.ic_logout),
                contentDescription = "Cerrar sesión",
                modifier = Modifier.padding(end = 8.dp),
            )
            Text("Cerrar sesión")
        }
    }

    if (errorMessage != null) {
        AlertDialog(
            onDismissRequest = { errorMessage = null },
            title = { Text("Atención") },
            text = { Text(errorMessage ?: "") },
            confirmButton = {
                TextButton(onClick = {
                    val shouldGoLogin = errorMessage == "Debe iniciar sesión nuevamente"
                    errorMessage = null
                    if (shouldGoLogin) {
                        SessionManager.clearSession()
                        onLogout()
                    }
                }) {
                    Text("Aceptar")
                }
            },
        )
    }
}

@Composable
private fun BillingResumeCard(
    data: BillingOverviewData,
    onOpenInvoiceDetail: () -> Unit,
    onGoToPayment: () -> Unit,
) {
    val factura = data.factura_actual

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Resumen de facturación",
                style = MaterialTheme.typography.titleMedium,
                color = AppColors.PrimaryDark,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Estado general de cuenta: ${data.estado_cuenta}",
                color = Color(0xFF5E6282),
                style = MaterialTheme.typography.bodyMedium,
            )

            Spacer(modifier = Modifier.height(12.dp))
            if (factura == null) {
                Text(
                    text = "No se encontró una factura vigente",
                    color = AppColors.NeutralText,
                )
            } else {
                Text("Factura actual: ${factura.numero_factura}", color = AppColors.PrimaryDark)
                Text("Período: ${factura.periodo_mes}/${factura.periodo_anio}", color = AppColors.NeutralText)
                Text("Subtotal: $${factura.subtotal}", color = AppColors.NeutralText)
                Text("Impuesto: $${factura.impuesto}", color = AppColors.NeutralText)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Total a pagar: $${factura.total}",
                    style = MaterialTheme.typography.headlineSmall,
                    color = AppColors.PrimaryDark,
                    fontWeight = FontWeight.Bold,
                )
                Text("Estado factura: ${factura.estado_factura}", color = AppColors.NeutralText)
            }

            Spacer(modifier = Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(
                    onClick = onOpenInvoiceDetail,
                    enabled = factura != null,
                    modifier = Modifier.weight(1f),
                ) {
                    Text("Ver detalle")
                }
                Button(
                    onClick = onGoToPayment,
                    enabled = factura != null,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = AppColors.Primary),
                ) {
                    Text("Pagar ahora")
                }
            }
        }
    }
}

@Composable
private fun SuscripcionesSection(suscripciones: List<SuscripcionData>) {
    Text(
        text = "Suscripciones",
        style = MaterialTheme.typography.titleMedium,
        color = AppColors.PrimaryDark,
        fontWeight = FontWeight.Bold,
    )
    Spacer(modifier = Modifier.height(8.dp))

    if (suscripciones.isEmpty()) {
        EmptyDataCard("No se encontraron suscripciones activas")
        return
    }

    suscripciones.forEach { suscripcion ->
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = suscripcion.nombre_plan,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = AppColors.PrimaryDark,
                        )
                        Text(
                            text = suscripcion.descripcion ?: "Sin descripción",
                            style = MaterialTheme.typography.bodySmall,
                            color = AppColors.NeutralText,
                        )
                    }
                    StatusChip(suscripcion.estado_suscripcion)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text("Inicio: ${suscripcion.fecha_inicio}")
                Text("Término: ${suscripcion.fecha_fin ?: "Sin término"}")
                Text("Renovación automática: ${if (suscripcion.renovacion_automatica) "Sí" else "No"}")
                Text("Precio mensual: $${suscripcion.precio_mensual}")
                Text("Límite de usuarios: ${suscripcion.limite_usuarios}")
            }
        }
    }
}

@Composable
private fun ServiciosSection(servicios: List<UsuarioServicioData>) {
    Text(
        text = "Servicios adicionales",
        style = MaterialTheme.typography.titleMedium,
        color = AppColors.PrimaryDark,
        fontWeight = FontWeight.Bold,
    )
    Spacer(modifier = Modifier.height(8.dp))

    if (servicios.isEmpty()) {
        EmptyDataCard("No tienes servicios adicionales contratados")
        return
    }

    servicios.forEach { servicio ->
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = servicio.nombre_servicio,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = AppColors.PrimaryDark,
                        )
                        Text(
                            text = servicio.descripcion ?: "Sin descripción",
                            style = MaterialTheme.typography.bodySmall,
                            color = AppColors.NeutralText,
                        )
                    }
                    StatusChip(servicio.estado)
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text("Costo mensual: $${servicio.costo_mensual}")
                Text("Precio pactado: $${servicio.precio_pactado}")
                Text("Contratación: ${servicio.fecha_contratacion}")
                Text("Término: ${servicio.fecha_termino ?: "Sin término"}")
            }
        }
    }
}

@Composable
private fun EmptyDataCard(message: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFFF2F3FA),
    ) {
        Text(
            text = message,
            modifier = Modifier.padding(14.dp),
            color = AppColors.NeutralText,
        )
    }
}

@Composable
private fun StatusChip(status: String) {
    val cleanStatus = status.uppercase()
    val (background, textColor) = when (cleanStatus) {
        "ACTIVA", "ACTIVO", "PAGADA" -> Color(0xFFDFF5E5) to Color(0xFF19713E)
        "VENCIDA", "SUSPENDIDA", "SUSPENDIDO" -> Color(0xFFFFF4D8) to Color(0xFF7D5B00)
        "CANCELADA", "ANULADA", "RECHAZADO" -> Color(0xFFFDE1DE) to Color(0xFF8C1D18)
        else -> Color(0xFFEAEAF0) to Color(0xFF4E5270)
    }

    Text(
        text = cleanStatus,
        modifier = Modifier
            .background(background, RoundedCornerShape(10.dp))
            .padding(horizontal = 10.dp, vertical = 5.dp),
        color = textColor,
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.SemiBold,
    )
}
