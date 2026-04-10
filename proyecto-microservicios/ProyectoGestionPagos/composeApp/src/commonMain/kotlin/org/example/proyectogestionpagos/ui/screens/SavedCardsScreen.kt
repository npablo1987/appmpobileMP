package org.example.proyectogestionpagos.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.unit.sp
import org.example.proyectogestionpagos.data.model.TarjetaResponse
import org.example.proyectogestionpagos.data.session.SessionManager
import org.example.proyectogestionpagos.ui.theme.AppColors
import org.example.proyectogestionpagos.ui.viewmodel.CardUiState
import org.example.proyectogestionpagos.ui.viewmodel.CardViewModel
import org.jetbrains.compose.resources.painterResource
import proyectogestionpagos.composeapp.generated.resources.Res
import proyectogestionpagos.composeapp.generated.resources.ic_home

@Composable
fun SavedCardsScreen(
    onBack: () -> Unit,
    onAddCard: () -> Unit,
    viewModel: CardViewModel = remember { CardViewModel() },
) {
    val idUsuario = SessionManager.idUsuario
    var showDeleteDialog by remember { mutableStateOf<TarjetaResponse?>(null) }
    var dialogMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(idUsuario) {
        if (idUsuario != null) {
            viewModel.cargarTarjetas(idUsuario)
        }
    }

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
            .padding(16.dp),
        horizontalAlignment = Alignment.Start,
    ) {
        TextButton(onClick = onBack) {
            Text("← Volver", color = AppColors.PrimaryDark)
        }

        Text(
            text = "Mis tarjetas",
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
                    "Debe iniciar sesión para ver sus tarjetas",
                    modifier = Modifier.padding(16.dp),
                    color = Color(0xFF8C1D18),
                    fontWeight = FontWeight.SemiBold,
                )
            }
            return@Column
        }

        Button(
            onClick = onAddCard,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = AppColors.Primary),
            shape = RoundedCornerShape(12.dp),
        ) {
            Text("+ Agregar nueva tarjeta", fontWeight = FontWeight.SemiBold, color = Color.White)
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (viewModel.isLoadingCards) {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = AppColors.Primary)
            }
        } else if (viewModel.tarjetas.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF4D8)),
            ) {
                Text(
                    "No tienes tarjetas guardadas",
                    modifier = Modifier.padding(16.dp),
                    color = Color(0xFF7D5B00),
                    fontWeight = FontWeight.SemiBold,
                )
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(viewModel.tarjetas) { tarjeta ->
                    CardItem(
                        tarjeta = tarjeta,
                        onSetDefault = {
                            viewModel.marcarTarjetaDefault(tarjeta.id, idUsuario)
                        },
                        onDelete = {
                            showDeleteDialog = tarjeta
                        }
                    )
                }
            }
        }
    }

    if (showDeleteDialog != null && idUsuario != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.eliminarTarjeta(showDeleteDialog!!.id, idUsuario!!)
                    showDeleteDialog = null
                }) {
                    Text("Eliminar", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = null }) {
                    Text("Cancelar")
                }
            },
            title = { Text("Eliminar tarjeta") },
            text = { Text("¿Está seguro que desea eliminar la tarjeta terminada en ${showDeleteDialog!!.last_four_digits}?") },
        )
    }

    if (dialogMessage != null) {
        AlertDialog(
            onDismissRequest = { dialogMessage = null },
            confirmButton = {
                TextButton(onClick = { dialogMessage = null }) {
                    Text("Aceptar")
                }
            },
            title = { Text("Tarjetas") },
            text = { Text(dialogMessage ?: "") },
        )
    }
}

@Composable
private fun CardItem(
    tarjeta: TarjetaResponse,
    onSetDefault: () -> Unit,
    onDelete: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (tarjeta.is_default) AppColors.Primary else Color(0xFFF2F3FA)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .background(
                            if (tarjeta.is_default) Color.White.copy(alpha = 0.3f) else AppColors.Primary.copy(alpha = 0.2f),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "💳",
                        fontSize = 24.sp
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = tarjeta.brand ?: "Tarjeta",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = if (tarjeta.is_default) Color.White else AppColors.PrimaryDark
                    )
                    Text(
                        text = "**** **** **** ${tarjeta.last_four_digits}",
                        fontSize = 14.sp,
                        color = if (tarjeta.is_default) Color.White.copy(alpha = 0.9f) else Color.Gray
                    )
                    Text(
                        text = "${tarjeta.expiration_month.toString().padStart(2, '0')}/${tarjeta.expiration_year}",
                        fontSize = 12.sp,
                        color = if (tarjeta.is_default) Color.White.copy(alpha = 0.8f) else Color.Gray
                    )
                    if (tarjeta.is_default) {
                        Text(
                            text = "Predeterminada",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFFB84D),
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }

            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                if (!tarjeta.is_default) {
                    TextButton(
                        onClick = onSetDefault,
                        modifier = Modifier.height(32.dp)
                    ) {
                        Text(
                            "Marcar como principal",
                            fontSize = 11.sp,
                            color = AppColors.Primary
                        )
                    }
                }
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(32.dp)
                ) {
                    Text("🗑️", fontSize = 18.sp)
                }
            }
        }
    }
}
