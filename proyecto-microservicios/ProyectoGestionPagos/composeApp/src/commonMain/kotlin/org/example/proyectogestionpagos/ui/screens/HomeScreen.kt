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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import org.example.proyectogestionpagos.data.model.HomeUserData
import org.example.proyectogestionpagos.data.network.AuthApiService
import org.example.proyectogestionpagos.data.session.SessionManager
import org.example.proyectogestionpagos.generated.resources.Res
import org.example.proyectogestionpagos.generated.resources.ic_badge
import org.example.proyectogestionpagos.generated.resources.ic_calendar_month
import org.example.proyectogestionpagos.generated.resources.ic_email
import org.example.proyectogestionpagos.generated.resources.ic_home
import org.example.proyectogestionpagos.generated.resources.ic_location_city
import org.example.proyectogestionpagos.generated.resources.ic_logout
import org.example.proyectogestionpagos.generated.resources.ic_phone
import org.example.proyectogestionpagos.generated.resources.ic_pin_drop
import org.example.proyectogestionpagos.generated.resources.ic_verified_user
import org.example.proyectogestionpagos.ui.components.AppBottomBar
import org.example.proyectogestionpagos.ui.theme.AppColors
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

@Composable
fun HomeScreen(
    onLogout: () -> Unit,
    onProfileClick: () -> Unit,
) {
    val authApiService = remember { AuthApiService() }
    var isLoading by remember { mutableStateOf(true) }
    var homeUserData by remember { mutableStateOf<HomeUserData?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        println("[HomeScreen] Cargando datos del usuario para Home")
        val idUsuario = SessionManager.idUsuario
        if (idUsuario == null) {
            println("[HomeScreen] Sesión inválida: id_usuario no encontrado")
            errorMessage = "Sesión inválida o expirada"
            isLoading = false
            return@LaunchedEffect
        }

        println("[HomeScreen] id_usuario recuperado correctamente: $idUsuario")
        val response = authApiService.getHomeUserData(idUsuario)

        if (response.success && response.data != null) {
            println("[HomeScreen] Datos cargados exitosamente")
            homeUserData = response.data
        } else {
            println("[HomeScreen] No fue posible obtener datos del usuario: ${response.message}")
            errorMessage = response.message
        }
        isLoading = false
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                painter = painterResource(Res.drawable.ic_home),
                contentDescription = "Inicio",
                tint = AppColors.PrimaryDark,
            )
            Text(
                text = "Bienvenido",
                modifier = Modifier.padding(start = 8.dp),
                style = MaterialTheme.typography.headlineMedium,
                color = AppColors.PrimaryDark,
                fontWeight = FontWeight.Bold,
            )
        }

        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = homeUserData?.nombre_completo ?: "Usuario",
            style = MaterialTheme.typography.titleMedium,
            color = Color(0xFF4E5270),
        )

        Spacer(modifier = Modifier.height(18.dp))

        when {
            isLoading -> {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                ) {
                    CircularProgressIndicator(color = AppColors.Primary)
                }
            }

            homeUserData != null -> {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    shadowElevation = 2.dp,
                    tonalElevation = 2.dp,
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        InfoRow(Res.drawable.ic_badge, "Nombre", homeUserData?.nombre_completo ?: "-")
                        InfoRow(Res.drawable.ic_email, "Correo", homeUserData?.correo ?: "-")
                        InfoRow(Res.drawable.ic_phone, "Teléfono", homeUserData?.telefono ?: "-")
                        InfoRow(Res.drawable.ic_location_city, "Ciudad", homeUserData?.ciudad ?: "-")
                        InfoRow(Res.drawable.ic_pin_drop, "Dirección", homeUserData?.direccion ?: "-")
                        InfoRow(Res.drawable.ic_calendar_month, "Fecha registro", homeUserData?.fecha_registro ?: "-")

                        Spacer(modifier = Modifier.height(10.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                painter = painterResource(Res.drawable.ic_verified_user),
                                contentDescription = "Estado de cuenta",
                                tint = Color(0xFF7A8198),
                            )
                            Text(
                                text = "Estado de cuenta",
                                modifier = Modifier.padding(start = 6.dp),
                                style = MaterialTheme.typography.labelMedium,
                                color = Color(0xFF7A8198),
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = homeUserData?.estado_cuenta ?: "-",
                            modifier = Modifier
                                .background(
                                    color = Color(0xFFDFF5E5),
                                    shape = RoundedCornerShape(10.dp),
                                )
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            color = Color(0xFF19713E),
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                AppBottomBar(
                    currentSection = "home",
                    onHomeClick = {},
                    onProfileClick = onProfileClick,
                )
            }

            else -> {
                Text(
                    text = "No fue posible cargar la información del usuario.",
                    color = Color(0xFFB3261E),
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
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
                    val shouldGoLogin = errorMessage == "Sesión inválida o expirada"
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
private fun InfoRow(icon: DrawableResource, label: String, value: String) {
    Column(modifier = Modifier.padding(bottom = 10.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                painter = painterResource(icon),
                contentDescription = label,
                tint = Color(0xFF7A8198),
            )
            Text(
                text = label,
                modifier = Modifier.padding(start = 6.dp),
                style = MaterialTheme.typography.labelMedium,
                color = Color(0xFF7A8198),
            )
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            color = AppColors.PrimaryDark,
            fontWeight = FontWeight.Medium,
        )
    }
}
