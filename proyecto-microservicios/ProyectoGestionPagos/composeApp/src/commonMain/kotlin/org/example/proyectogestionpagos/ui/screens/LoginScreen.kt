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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import org.example.proyectogestionpagos.data.network.AuthApiService
import org.example.proyectogestionpagos.data.session.SessionManager
import org.example.proyectogestionpagos.ui.components.LottieAnimation
import proyectogestionpagos.composeapp.generated.resources.Res
import proyectogestionpagos.composeapp.generated.resources.ic_email
import proyectogestionpagos.composeapp.generated.resources.ic_lock
import proyectogestionpagos.composeapp.generated.resources.ic_login
import proyectogestionpagos.composeapp.generated.resources.ic_visibility
import proyectogestionpagos.composeapp.generated.resources.ic_visibility_off
import org.example.proyectogestionpagos.ui.theme.AppColors
import org.jetbrains.compose.resources.painterResource

@Composable
fun LoginScreen(onLoginSuccess: () -> Unit) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showPassword by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()
    val authApiService = remember { AuthApiService() }

    fun validateInput(): String? {
        println("[LoginScreen] Iniciando validación de credenciales")
        val sanitizedEmail = email.trim()
        val sanitizedPassword = password.trim()

        if (sanitizedEmail.isEmpty()) return "Debe ingresar su correo"
        if (sanitizedPassword.isEmpty()) return "Debe ingresar su clave"

        val emailRegex = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")
        if (!emailRegex.matches(sanitizedEmail)) return "Correo con formato inválido"

        println("[LoginScreen] Validación local exitosa")
        return null
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFF8F9FA),
                        Color(0xFFFFFFFF)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start
            ) {
                LottieAnimation(
                    modifier = Modifier.size(260.dp)
                )

                Spacer(modifier = Modifier.width(12.dp))
                
                Column {
                    Text(
                        text = "RockerAPP",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = AppColors.Primary,
                        fontSize = 28.sp
                    )
                    Text(
                        text = "Pagos",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium,
                        color = AppColors.PrimaryDark,
                        fontSize = 18.sp
                    )
                }
            }


            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(
                        elevation = 8.dp,
                        shape = RoundedCornerShape(20.dp),
                        spotColor = Color.Black.copy(alpha = 0.1f)
                    )
                    .background(
                        color = Color.White,
                        shape = RoundedCornerShape(20.dp)
                    )
                    .padding(24.dp)
            ) {
                Text(
                    text = "Iniciar sesión",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.PrimaryDark,
                    fontSize = 26.sp
                )

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Ingresa tus credenciales para continuar",
                    color = Color(0xFF6B7280),
                    style = MaterialTheme.typography.bodyMedium,
                    fontSize = 14.sp
                )

                Spacer(modifier = Modifier.height(28.dp))

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Correo electrónico", fontSize = 14.sp) },
                    placeholder = { Text("ejemplo@email.com") },
                    singleLine = true,
                    leadingIcon = {
                        Icon(
                            painter = painterResource(Res.drawable.ic_email),
                            contentDescription = "Correo",
                            tint = AppColors.Primary
                        )
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    enabled = !isLoading,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                )

                Spacer(modifier = Modifier.height(20.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Contraseña", fontSize = 14.sp) },
                    placeholder = { Text("Tu contraseña") },
                    singleLine = true,
                    leadingIcon = {
                        Icon(
                            painter = painterResource(Res.drawable.ic_lock),
                            contentDescription = "Clave",
                            tint = AppColors.Primary
                        )
                    },
                    trailingIcon = {
                        IconButton(onClick = { showPassword = !showPassword }) {
                            Icon(
                                painter = painterResource(if (showPassword) Res.drawable.ic_visibility_off else Res.drawable.ic_visibility),
                                contentDescription = if (showPassword) "Ocultar clave" else "Mostrar clave",
                                tint = Color(0xFF6B7280)
                            )
                        }
                    },
                    visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                    enabled = !isLoading,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                )

                Spacer(modifier = Modifier.height(28.dp))

                Button(
                    onClick = {
                if (isLoading) return@Button

                val validationError = validateInput()
                if (validationError != null) {
                    errorMessage = validationError
                    println("[LoginScreen] Error validación local: $validationError")
                    return@Button
                }

                coroutineScope.launch {
                    isLoading = true
                    try {
                        println("[LoginScreen] Llamando endpoint /auth/login")
                        val response = authApiService.login(
                            correo = email.trim(),
                            clave = password.trim(),
                        )

                        if (response.success && response.data != null) {
                            SessionManager.saveSession(response.data)
                            println("[LoginScreen] Login exitoso, navegando a Home")
                            onLoginSuccess()
                        } else {
                            println("[LoginScreen] Error de autenticación: ${response.message}")
                            errorMessage = response.message
                        }
                    } catch (exception: Exception) {
                        println("[LoginScreen] Error de conexión con servidor: ${exception.message}")
                        errorMessage = "No fue posible conectar con el servidor"
                    } finally {
                        isLoading = false
                    }
                    }
                },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .shadow(
                            elevation = 4.dp,
                            shape = RoundedCornerShape(12.dp),
                            spotColor = AppColors.Primary.copy(alpha = 0.3f)
                        ),
                    shape = RoundedCornerShape(12.dp),
                    enabled = !isLoading,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AppColors.Primary,
                        contentColor = Color.White,
                    ),
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            color = Color.White,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(24.dp),
                        )
                    } else {
                        Icon(
                            painter = painterResource(Res.drawable.ic_login),
                            contentDescription = "Iniciar sesión",
                            modifier = Modifier.padding(end = 8.dp),
                        )
                        Text(
                            "Iniciar sesión",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "© 2026 RockerAPP - Todos los derechos reservados",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF9CA3AF),
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }

    if (errorMessage != null) {
        AlertDialog(
            onDismissRequest = { errorMessage = null },
            title = { Text("Atención") },
            text = { Text(errorMessage ?: "") },
            confirmButton = {
                TextButton(onClick = { errorMessage = null }) {
                    Text("Aceptar")
                }
            },
        )
    }
}
