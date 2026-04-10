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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.*
import androidx.compose.ui.unit.sp
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
    onGoToSavedCards: () -> Unit,
    onPayWithSavedCard: () -> Unit,
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
                    onPayWithSavedCard = onPayWithSavedCard,
                    onGoToSavedCards = onGoToSavedCards,
                )

                Spacer(modifier = Modifier.height(16.dp))
                QuickActionsSection(onProfileClick = onProfileClick)

                Spacer(modifier = Modifier.height(16.dp))
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
    onPayWithSavedCard: () -> Unit,
    onGoToSavedCards: () -> Unit,
) {
    val factura = data.factura_actual
    val suscripcion = data.suscripciones.firstOrNull()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(340.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = AppColors.Primary),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(AppColors.Primary)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = suscripcion?.nombre_plan ?: "Plan",
                            style = MaterialTheme.typography.titleLarge,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            text = suscripcion?.descripcion ?: "Suscripción activa",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.9f),
                        )
                    }
                    StatusChipWhite(factura?.estado_factura ?: "ACTIVO")
                }

                if (factura != null) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            InfoField("STB No", factura.numero_factura, Color.White)
                            InfoField("Plan Amount", "$${factura.total}", Color(0xFFFFB84D))
                            InfoField("Expiry Date", "${factura.periodo_mes}/${factura.periodo_anio}", Color.White)
                        }
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = onGoToPayment,
                        enabled = factura != null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFE53935),
                            disabledContainerColor = Color.Gray
                        ),
                        shape = RoundedCornerShape(12.dp),
                    ) {
                        Text(
                            "PAY WITH MERCADO PAGO",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.White
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = onPayWithSavedCard,
                            enabled = factura != null,
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = Color.White,
                                disabledContentColor = Color.Gray
                            ),
                            shape = RoundedCornerShape(10.dp),
                        ) {
                            Text(
                                "💳 Saved Card",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        OutlinedButton(
                            onClick = onGoToSavedCards,
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(10.dp),
                        ) {
                            Text(
                                "⚙️ Manage",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoField(label: String, value: String, color: Color) {
    Column(
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = color.copy(alpha = 0.8f),
            fontWeight = FontWeight.SemiBold,
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            color = color,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun StatusChipWhite(status: String) {
    val cleanStatus = status.uppercase()
    val backgroundColor = when (cleanStatus) {
        "ACTIVA", "ACTIVO", "PAGADA" -> Color(0xFF4CAF50)
        "VENCIDA", "SUSPENDIDA", "SUSPENDIDO" -> Color(0xFFFFC107)
        "CANCELADA", "ANULADA", "RECHAZADO" -> Color(0xFFE53935)
        else -> Color(0xFF757575)
    }

    Text(
        text = cleanStatus,
        modifier = Modifier
            .background(backgroundColor, RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        color = Color.White,
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.SemiBold,
    )
}

@Composable
private fun SuscripcionesSection(suscripciones: List<SuscripcionData>) {
    Text(
        text = "Suscripciones",
        style = MaterialTheme.typography.titleMedium,
        color = AppColors.PrimaryDark,
        fontWeight = FontWeight.Bold,
    )
    Spacer(modifier = Modifier.height(12.dp))

    if (suscripciones.isEmpty()) {
        EmptyDataCard("No se encontraron suscripciones activas")
        return
    }

    suscripciones.forEach { suscripcion ->
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(AppColors.Primary, RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "📺",
                                style = MaterialTheme.typography.headlineSmall,
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = suscripcion.nombre_plan,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = AppColors.PrimaryDark,
                            )
                            Text(
                                text = suscripcion.descripcion ?: "Sin descripción",
                                style = MaterialTheme.typography.bodySmall,
                                color = AppColors.NeutralText,
                            )
                        }
                    }
                    StatusChip(suscripcion.estado_suscripcion)
                }
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    DetailItem("Inicio", suscripcion.fecha_inicio)
                    DetailItem("Término", suscripcion.fecha_fin ?: "Sin término")
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    DetailItem("Precio", "$${suscripcion.precio_mensual}/mes")
                    DetailItem("Usuarios", "${suscripcion.limite_usuarios}")
                }
            }
        }
    }
}

@Composable
private fun DetailItem(label: String, value: String) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = AppColors.NeutralText,
            fontWeight = FontWeight.SemiBold,
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            color = AppColors.PrimaryDark,
            fontWeight = FontWeight.SemiBold,
        )
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
    Spacer(modifier = Modifier.height(12.dp))

    if (servicios.isEmpty()) {
        EmptyDataCard("No tienes servicios adicionales contratados")
        return
    }

    servicios.forEach { servicio ->
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(Color(0xFF4CAF50), RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "⚙️",
                                style = MaterialTheme.typography.headlineSmall,
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = servicio.nombre_servicio,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = AppColors.PrimaryDark,
                            )
                            Text(
                                text = servicio.descripcion ?: "Sin descripción",
                                style = MaterialTheme.typography.bodySmall,
                                color = AppColors.NeutralText,
                            )
                        }
                    }
                    StatusChip(servicio.estado)
                }
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    DetailItem("Costo", "$${servicio.costo_mensual}/mes")
                    DetailItem("Pactado", "$${servicio.precio_pactado}")
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    DetailItem("Contratación", servicio.fecha_contratacion)
                    DetailItem("Término", servicio.fecha_termino ?: "Sin término")
                }
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
private fun QuickActionsSection(onProfileClick: () -> Unit) {
    Column {
        Text(
            text = "Quick Actions",
            style = MaterialTheme.typography.titleMedium,
            color = AppColors.PrimaryDark,
            fontWeight = FontWeight.Bold,
        )
        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            QuickActionButton(
                icon = "🔔",
                label = "NOTIFICATION",
                backgroundColor = Color(0xFFE91E63),
                modifier = Modifier.weight(1f)
            )
            QuickActionButton(
                icon = "👤",
                label = "PROFILE",
                backgroundColor = Color(0xFFFFC107),
                modifier = Modifier.weight(1f),
                onClick = onProfileClick
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            QuickActionButton(
                icon = "💬",
                label = "SERVICE COMPLAINTS",
                backgroundColor = Color(0xFF00BCD4),
                modifier = Modifier.weight(1f)
            )
            QuickActionButton(
                icon = "📊",
                label = "REPORTS",
                backgroundColor = Color(0xFF4CAF50),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun QuickActionButton(
    icon: String,
    label: String,
    backgroundColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = modifier
            .height(120.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = icon,
                style = MaterialTheme.typography.headlineSmall,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
            )
        }
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
