package org.example.proyectogestionpagos.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.example.proyectogestionpagos.data.session.SessionManager
import org.example.proyectogestionpagos.ui.theme.AppColors
import org.example.proyectogestionpagos.ui.viewmodel.CardUiState
import org.example.proyectogestionpagos.ui.viewmodel.CardViewModel

@Composable
fun AddCardScreen(
    onBack: () -> Unit,
    viewModel: CardViewModel = remember { CardViewModel() },
) {
    val idUsuario = SessionManager.idUsuario
    var cardNumber by remember { mutableStateOf("") }
    var cardHolder by remember { mutableStateOf("") }
    var expiryMonth by remember { mutableStateOf("") }
    var expiryYear by remember { mutableStateOf("") }
    var cvv by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var dialogMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(viewModel.uiState) {
        when (val state = viewModel.uiState) {
            is CardUiState.Success -> {
                dialogMessage = state.message
                viewModel.resetUiState()
            }
            is CardUiState.Error -> {
                dialogMessage = state.message
                viewModel.resetUiState()
            }
            else -> {}
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
            text = "Agregar tarjeta",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = AppColors.PrimaryDark,
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (idUsuario == null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFDE1DE)),
            ) {
                Text(
                    "Debe iniciar sesión para agregar tarjetas",
                    modifier = Modifier.padding(16.dp),
                    color = Color(0xFF8C1D18),
                    fontWeight = FontWeight.SemiBold,
                )
            }
            return@Column
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF4D8)),
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "⚠️ Modo de prueba",
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF7D5B00),
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Usa tarjetas de prueba de Mercado Pago. Ejemplo:\n" +
                            "Visa: 4509 9535 6623 3704\n" +
                            "Mastercard: 5031 7557 3453 0604\n" +
                            "CVV: cualquier 3 dígitos\n" +
                            "Fecha: cualquier fecha futura",
                    fontSize = 12.sp,
                    color = Color(0xFF7D5B00),
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        OutlinedTextField(
            value = cardNumber,
            onValueChange = { if (it.length <= 19) cardNumber = it.filter { c -> c.isDigit() || c == ' ' } },
            label = { Text("Número de tarjeta") },
            placeholder = { Text("1234 5678 9012 3456") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = cardHolder,
            onValueChange = { cardHolder = it },
            label = { Text("Nombre del titular") },
            placeholder = { Text("JUAN PEREZ") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = expiryMonth,
            onValueChange = { if (it.length <= 2) expiryMonth = it.filter { c -> c.isDigit() } },
            label = { Text("Mes de vencimiento (MM)") },
            placeholder = { Text("12") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = expiryYear,
            onValueChange = { if (it.length <= 4) expiryYear = it.filter { c -> c.isDigit() } },
            label = { Text("Año de vencimiento (YYYY)") },
            placeholder = { Text("2025") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = cvv,
            onValueChange = { if (it.length <= 4) cvv = it.filter { c -> c.isDigit() } },
            label = { Text("CVV") },
            placeholder = { Text("123") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            placeholder = { Text("usuario@email.com") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            singleLine = true,
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                if (cardNumber.isEmpty() || cardHolder.isEmpty() || expiryMonth.isEmpty() || 
                    expiryYear.isEmpty() || cvv.isEmpty() || email.isEmpty()) {
                    dialogMessage = "Por favor complete todos los campos"
                    return@Button
                }

                val token = "MOCK_TOKEN_${cardNumber.replace(" ", "")}_${System.currentTimeMillis()}"
                
                viewModel.guardarTarjeta(idUsuario, token, email) {
                    onBack()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = AppColors.Primary),
            shape = RoundedCornerShape(12.dp),
            enabled = viewModel.uiState !is CardUiState.Loading
        ) {
            if (viewModel.uiState is CardUiState.Loading) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.padding(8.dp))
            } else {
                Text("Guardar tarjeta", fontWeight = FontWeight.SemiBold, color = Color.White)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFDFF5E5)),
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "🔒 Seguridad",
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF19713E),
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Los datos de tu tarjeta son procesados de forma segura por Mercado Pago. " +
                            "No almacenamos tu número de tarjeta completo ni tu CVV.",
                    fontSize = 12.sp,
                    color = Color(0xFF19713E),
                )
            }
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
            title = { Text("Agregar tarjeta") },
            text = { Text(dialogMessage ?: "") },
        )
    }
}
