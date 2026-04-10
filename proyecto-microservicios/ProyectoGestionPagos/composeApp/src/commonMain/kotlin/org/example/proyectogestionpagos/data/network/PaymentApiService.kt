package org.example.proyectogestionpagos.data.network

import io.ktor.client.call.body
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.plugins.ResponseException
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import org.example.proyectogestionpagos.getPaymentsBaseUrl
import org.example.proyectogestionpagos.data.model.CrearPagoRequest
import org.example.proyectogestionpagos.data.model.CrearPagoResponse
import org.example.proyectogestionpagos.data.model.EstadoPagoResponse
import org.example.proyectogestionpagos.data.model.PagoDirectoRequest
import org.example.proyectogestionpagos.data.model.PagoDirectoResponse

class PaymentApiService {
    private val client = ApiClient.httpClient
    private val pagosBaseUrl = getPaymentsBaseUrl()

    suspend fun crearPago(idUsuario: Int, descripcion: String, monto: Double): CrearPagoResponse {
        println("[PaymentApiService] inicio de pago id_usuario=$idUsuario monto=$monto")
        return try {
            val response = client.post("$pagosBaseUrl/pagos/crear") {
                contentType(ContentType.Application.Json)
                setBody(CrearPagoRequest(id_usuario = idUsuario, descripcion = descripcion, monto = monto))
            }
            response.body()
        } catch (exception: ClientRequestException) {
            println("[PaymentApiService] error creando pago: ${exception.message}")
            CrearPagoResponse(success = false, message = "Solicitud inválida para crear pago")
        } catch (exception: HttpRequestTimeoutException) {
            println("[PaymentApiService] timeout creando pago")
            CrearPagoResponse(success = false, message = "Timeout al crear pago")
        } catch (exception: ResponseException) {
            println("[PaymentApiService] error HTTP creando pago: ${exception.message}")
            CrearPagoResponse(success = false, message = "Error de servidor de pagos")
        } catch (exception: Exception) {
            println("[PaymentApiService] error de red creando pago: ${exception.message}")
            CrearPagoResponse(success = false, message = "No fue posible conectar con pagos")
        }
    }

    suspend fun consultarEstado(idPago: Int): EstadoPagoResponse? {
        println("[PaymentApiService] consultar estado id_pago=$idPago")
        return try {
            val response = client.get("$pagosBaseUrl/pagos/$idPago/estado")
            response.body()
        } catch (exception: Exception) {
            println("[PaymentApiService] error consultando estado: ${exception.message}")
            null
        }
    }

    suspend fun pagarDirecto(request: PagoDirectoRequest): PagoDirectoResponse {
        println("[PaymentApiService] pago directo id_usuario=${request.id_usuario} monto=${request.monto}")
        return try {
            val response = client.post("$pagosBaseUrl/pagos/directo/procesar") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }
            response.body()
        } catch (exception: ClientRequestException) {
            println("[PaymentApiService] error pago directo: ${exception.message}")
            PagoDirectoResponse(success = false, message = "Error en solicitud de pago")
        } catch (exception: HttpRequestTimeoutException) {
            println("[PaymentApiService] timeout pago directo")
            PagoDirectoResponse(success = false, message = "Timeout procesando pago")
        } catch (exception: ResponseException) {
            println("[PaymentApiService] error HTTP pago directo: ${exception.message}")
            PagoDirectoResponse(success = false, message = "Error en servidor de pagos")
        } catch (exception: Exception) {
            println("[PaymentApiService] error pago directo: ${exception.message}")
            PagoDirectoResponse(success = false, message = "No fue posible procesar el pago")
        }
    }
}
