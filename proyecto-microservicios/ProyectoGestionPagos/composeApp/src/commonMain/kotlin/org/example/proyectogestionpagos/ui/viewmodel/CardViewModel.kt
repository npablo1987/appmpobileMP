package org.example.proyectogestionpagos.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.example.proyectogestionpagos.data.model.TarjetaResponse
import org.example.proyectogestionpagos.data.repository.CardRepository
import org.example.proyectogestionpagos.data.service.MercadoPagoCardService

sealed class CardUiState {
    object Idle : CardUiState()
    object Loading : CardUiState()
    data class Success(val message: String) : CardUiState()
    data class Error(val message: String) : CardUiState()
}

class CardViewModel {
    private val repository = CardRepository()
    private val viewModelScope = CoroutineScope(Dispatchers.Main)

    var uiState by mutableStateOf<CardUiState>(CardUiState.Idle)
        private set

    var tarjetas by mutableStateOf<List<TarjetaResponse>>(emptyList())
        private set

    var isLoadingCards by mutableStateOf(false)
        private set

    fun guardarTarjeta(
        idUsuario: Int,
        cardNumber: String,
        cardholderName: String,
        expirationMonth: String,
        expirationYear: String,
        securityCode: String,
        email: String,
        onSuccess: () -> Unit
    ) {
        uiState = CardUiState.Loading
        viewModelScope.launch {
            // Generar token válido de Mercado Pago
            val token = MercadoPagoCardService.createCardToken(
                cardNumber = cardNumber.replace(" ", ""),
                cardholderName = cardholderName,
                expirationMonth = expirationMonth,
                expirationYear = expirationYear,
                securityCode = securityCode
            )
            
            if (token == null) {
                uiState = CardUiState.Error("No fue posible generar token. Verifica los datos de la tarjeta")
                return@launch
            }
            
            val response = repository.guardarTarjeta(idUsuario, token, email)
            if (response.success) {
                uiState = CardUiState.Success(response.message)
                cargarTarjetas(idUsuario)
                onSuccess()
            } else {
                uiState = CardUiState.Error(response.message)
            }
        }
    }

    fun cargarTarjetas(idUsuario: Int) {
        isLoadingCards = true
        viewModelScope.launch {
            val response = repository.listarTarjetas(idUsuario)
            if (response != null && response.success) {
                tarjetas = response.data
            } else {
                tarjetas = emptyList()
            }
            isLoadingCards = false
        }
    }

    fun marcarTarjetaDefault(idTarjeta: Int, idUsuario: Int) {
        viewModelScope.launch {
            val response = repository.marcarTarjetaDefault(idTarjeta, true)
            if (response != null && response.success) {
                cargarTarjetas(idUsuario)
                uiState = CardUiState.Success("Tarjeta marcada como predeterminada")
            } else {
                uiState = CardUiState.Error("No fue posible actualizar la tarjeta")
            }
        }
    }

    fun eliminarTarjeta(idTarjeta: Int, idUsuario: Int) {
        viewModelScope.launch {
            val response = repository.eliminarTarjeta(idTarjeta, idUsuario)
            if (response != null && response.success) {
                cargarTarjetas(idUsuario)
                uiState = CardUiState.Success("Tarjeta eliminada correctamente")
            } else {
                uiState = CardUiState.Error("No fue posible eliminar la tarjeta")
            }
        }
    }

    fun resetUiState() {
        uiState = CardUiState.Idle
    }
}
