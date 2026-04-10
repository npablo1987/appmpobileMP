package org.example.proyectogestionpagos.data.service

import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.example.proyectogestionpagos.data.network.ApiClient

/**
 * Servicio para generar tokens de tarjeta con Mercado Pago
 * Los tokens se generan mediante la API public de Mercado Pago
 */
object MercadoPagoCardService {
    
    // Public key de Mercado Pago para el entorno de prueba
    private const val PUBLIC_KEY = "TEST-3361de71-ab52-4f15-bec3-9ecfaf855528"
    private const val MP_CARD_TOKENS_URL = "https://api.mercadopago.com/v1/card_tokens"
    
    /**
     * Genera un token de tarjeta válido para Mercado Pago
     * @param cardNumber Número de tarjeta (sin espacios)
     * @param cardholderName Nombre del titular
     * @param expirationMonth Mes de vencimiento (MM)
     * @param expirationYear Año de vencimiento (YYYY)
     * @param securityCode CVV/CVC
     * @return Token válido o null si hay error
     */
    suspend fun createCardToken(
        cardNumber: String,
        cardholderName: String,
        expirationMonth: String,
        expirationYear: String,
        securityCode: String
    ): String? {
        return try {
            val client = ApiClient.httpClient
            
            val cardData = CardTokenRequest(
                cardNumber = cardNumber,
                cardholderName = cardholderName,
                cardholderIdentification = CardholderIdentification(
                    type = "DNI",
                    number = "123456789"
                ),
                securityCode = securityCode,
                expirationMonth = expirationMonth,
                expirationYear = expirationYear,
                publicKey = PUBLIC_KEY
            )
            
            val response = client.post(MP_CARD_TOKENS_URL) {
                contentType(ContentType.Application.Json)
                setBody(cardData)
            }
            
            if (response.status.value == 200) {
                val tokenResponse: CardTokenResponse = response.body()
                println("[MercadoPagoCardService] Token generado exitosamente: ${tokenResponse.id}")
                return tokenResponse.id
            } else {
                println("[MercadoPagoCardService] Error al generar token: ${response.status}")
                return null
            }
        } catch (e: Exception) {
            println("[MercadoPagoCardService] Excepción generando token: ${e.message}")
            e.printStackTrace()
            return null
        }
    }
    
    @Serializable
    data class CardTokenRequest(
        @SerialName("card_number")
        val cardNumber: String,
        @SerialName("cardholder")
        val cardholderName: String? = null,
        @SerialName("security_code")
        val securityCode: String,
        @SerialName("expiration_month")
        val expirationMonth: String,
        @SerialName("expiration_year")
        val expirationYear: String,
        @SerialName("cardholder_identification")
        val cardholderIdentification: CardholderIdentification? = null,
        @SerialName("public_key")
        val publicKey: String
    )
    
    @Serializable
    data class CardholderIdentification(
        val type: String,
        val number: String
    )
    
    @Serializable
    data class CardTokenResponse(
        val id: String,
        @SerialName("card_id")
        val cardId: String? = null,
        val status: String? = null,
        val message: String? = null
    )
}
