package org.example.proyectogestionpagos.data.network

import io.ktor.client.call.body
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.plugins.ResponseException
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import org.example.proyectogestionpagos.getPaymentsBaseUrl
import org.example.proyectogestionpagos.data.model.GuardarTarjetaRequest
import org.example.proyectogestionpagos.data.model.GuardarTarjetaResponse
import org.example.proyectogestionpagos.data.model.PagoConTarjetaGuardadaRequest
import org.example.proyectogestionpagos.data.model.PagoConTarjetaGuardadaResponse
import org.example.proyectogestionpagos.data.model.TarjetaDeleteResponse
import org.example.proyectogestionpagos.data.model.TarjetaSetDefaultRequest
import org.example.proyectogestionpagos.data.model.TarjetasListResponse

class CardApiService {
    private val client = ApiClient.httpClient
    private val pagosBaseUrl = getPaymentsBaseUrl()

    suspend fun guardarTarjeta(idUsuario: Int, token: String, email: String): GuardarTarjetaResponse {
        println("[CardApiService] Guardando tarjeta id_usuario=$idUsuario")
        return try {
            val response = client.post("$pagosBaseUrl/tarjetas/guardar") {
                contentType(ContentType.Application.Json)
                setBody(GuardarTarjetaRequest(id_usuario = idUsuario, token = token, email = email))
            }
            response.body()
        } catch (exception: ClientRequestException) {
            println("[CardApiService] Error guardando tarjeta: ${exception.message}")
            GuardarTarjetaResponse(success = false, message = "Solicitud inválida para guardar tarjeta")
        } catch (exception: HttpRequestTimeoutException) {
            println("[CardApiService] Timeout guardando tarjeta")
            GuardarTarjetaResponse(success = false, message = "Timeout al guardar tarjeta")
        } catch (exception: ResponseException) {
            println("[CardApiService] Error HTTP guardando tarjeta: ${exception.message}")
            GuardarTarjetaResponse(success = false, message = "Error de servidor al guardar tarjeta")
        } catch (exception: Exception) {
            println("[CardApiService] Error de red guardando tarjeta: ${exception.message}")
            GuardarTarjetaResponse(success = false, message = "No fue posible conectar con el servidor")
        }
    }

    suspend fun listarTarjetas(idUsuario: Int): TarjetasListResponse? {
        println("[CardApiService] Listando tarjetas id_usuario=$idUsuario")
        return try {
            val response = client.get("$pagosBaseUrl/tarjetas/usuario/$idUsuario")
            response.body()
        } catch (exception: Exception) {
            println("[CardApiService] Error listando tarjetas: ${exception.message}")
            null
        }
    }

    suspend fun marcarTarjetaDefault(idTarjeta: Int, isDefault: Boolean): GuardarTarjetaResponse? {
        println("[CardApiService] Marcando tarjeta como default id_tarjeta=$idTarjeta is_default=$isDefault")
        return try {
            val response = client.patch("$pagosBaseUrl/tarjetas/$idTarjeta/default") {
                contentType(ContentType.Application.Json)
                setBody(TarjetaSetDefaultRequest(is_default = isDefault))
            }
            response.body()
        } catch (exception: Exception) {
            println("[CardApiService] Error marcando tarjeta default: ${exception.message}")
            null
        }
    }

    suspend fun eliminarTarjeta(idTarjeta: Int, idUsuario: Int): TarjetaDeleteResponse? {
        println("[CardApiService] Eliminando tarjeta id_tarjeta=$idTarjeta id_usuario=$idUsuario")
        return try {
            val response = client.delete("$pagosBaseUrl/tarjetas/$idTarjeta") {
                parameter("id_usuario", idUsuario)
            }
            response.body()
        } catch (exception: Exception) {
            println("[CardApiService] Error eliminando tarjeta: ${exception.message}")
            null
        }
    }

    suspend fun pagarConTarjetaGuardada(
        idUsuario: Int,
        idTarjeta: Int,
        descripcion: String,
        monto: Double
    ): PagoConTarjetaGuardadaResponse {
        println("[CardApiService] Pago con tarjeta guardada id_usuario=$idUsuario id_tarjeta=$idTarjeta monto=$monto")
        return try {
            val response = client.post("$pagosBaseUrl/tarjetas/pagar") {
                contentType(ContentType.Application.Json)
                setBody(
                    PagoConTarjetaGuardadaRequest(
                        id_usuario = idUsuario,
                        id_tarjeta = idTarjeta,
                        descripcion = descripcion,
                        monto = monto
                    )
                )
            }
            response.body()
        } catch (exception: ClientRequestException) {
            println("[CardApiService] Error pagando con tarjeta: ${exception.message}")
            PagoConTarjetaGuardadaResponse(success = false, message = "Solicitud inválida para pagar")
        } catch (exception: HttpRequestTimeoutException) {
            println("[CardApiService] Timeout pagando con tarjeta")
            PagoConTarjetaGuardadaResponse(success = false, message = "Timeout al procesar pago")
        } catch (exception: ResponseException) {
            println("[CardApiService] Error HTTP pagando con tarjeta: ${exception.message}")
            PagoConTarjetaGuardadaResponse(success = false, message = "Error de servidor al procesar pago")
        } catch (exception: Exception) {
            println("[CardApiService] Error de red pagando con tarjeta: ${exception.message}")
            PagoConTarjetaGuardadaResponse(success = false, message = "No fue posible conectar con el servidor")
        }
    }
}
