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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.example.proyectogestionpagos.data.model.HomeUserData
import org.example.proyectogestionpagos.data.network.AuthApiService
import org.example.proyectogestionpagos.data.session.SessionManager
import org.example.proyectogestionpagos.ui.theme.AppColors

@Composable
fun HomeScreen(onLogout: () -> Unit) {
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
        Text(
            text = "Bienvenido",
            style = MaterialTheme.typography.headlineMedium,
            color = AppColors.PrimaryDark,
            fontWeight = FontWeight.Bold,
        )

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
                        InfoRow("Nombre", homeUserData?.nombre_completo ?: "-")
                        InfoRow("Correo", homeUserData?.correo ?: "-")
                        InfoRow("Teléfono", homeUserData?.telefono ?: "-")
                        InfoRow("Ciudad", homeUserData?.ciudad ?: "-")
                        InfoRow("Dirección", homeUserData?.direccion ?: "-")
                        InfoRow("Fecha registro", homeUserData?.fecha_registro ?: "-")

                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Estado de cuenta",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color(0xFF7A8198),
                        )
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
private fun InfoRow(label: String, value: String) {
    Column(modifier = Modifier.padding(bottom = 10.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = Color(0xFF7A8198),
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            color = AppColors.PrimaryDark,
            fontWeight = FontWeight.Medium,
        )
    }
}
