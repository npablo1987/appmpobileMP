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
}
