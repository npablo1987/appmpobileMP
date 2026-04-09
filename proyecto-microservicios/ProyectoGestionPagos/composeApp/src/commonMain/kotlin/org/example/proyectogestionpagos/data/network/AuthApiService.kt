package org.example.proyectogestionpagos.data.network

import io.ktor.client.call.body
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.plugins.ResponseException
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.contentType
import io.ktor.http.ContentType
import org.example.proyectogestionpagos.getApiBaseUrl
import org.example.proyectogestionpagos.data.model.HomeResponse
import org.example.proyectogestionpagos.data.model.BillingOverviewResponse
import org.example.proyectogestionpagos.data.model.LoginRequest
import org.example.proyectogestionpagos.data.model.LoginResponse
import org.example.proyectogestionpagos.data.model.PreparePaymentRequest
import org.example.proyectogestionpagos.data.model.PreparePaymentResponse

class AuthApiService {
    private val client = ApiClient.httpClient
    private val baseUrl = getApiBaseUrl()

    suspend fun login(correo: String, clave: String): LoginResponse {
        println("[AuthApiService] Consumiendo endpoint /auth/login")
        return try {
            val response = client.post("$baseUrl/auth/login") {
                contentType(ContentType.Application.Json)
                setBody(LoginRequest(correo = correo.trim(), clave = clave.trim()))
            }
            response.body()
        } catch (exception: ClientRequestException) {
            val errorBody = exception.response.body<LoginResponse>()
            println("[AuthApiService] Error autenticación: ${errorBody.message}")
            errorBody
        } catch (exception: HttpRequestTimeoutException) {
            println("[AuthApiService] Timeout conexión servidor")
            LoginResponse(success = false, message = "No fue posible conectar con el servidor")
        } catch (exception: ResponseException) {
            println("[AuthApiService] Error HTTP inesperado: ${exception.message}")
            LoginResponse(success = false, message = "Error del servidor")
        } catch (exception: Exception) {
            println("[AuthApiService] Error de red: ${exception.message}")
            LoginResponse(success = false, message = "No fue posible conectar con el servidor")
        }
    }

    suspend fun getHomeUserData(idUsuario: Int): HomeResponse {
        println("[AuthApiService] Consumiendo endpoint /auth/me/$idUsuario")
        return try {
            val response = client.get("$baseUrl/auth/me/$idUsuario")
            response.body()
        } catch (exception: ClientRequestException) {
            val errorBody = exception.response.body<HomeResponse>()
            println("[AuthApiService] Error carga Home: ${errorBody.message}")
            errorBody
        } catch (exception: HttpRequestTimeoutException) {
            println("[AuthApiService] Timeout conexión servidor en Home")
            HomeResponse(success = false, message = "No fue posible conectar con el servidor")
        } catch (exception: ResponseException) {
            println("[AuthApiService] Error HTTP inesperado Home: ${exception.message}")
            HomeResponse(success = false, message = "Error del servidor")
        } catch (exception: Exception) {
            println("[AuthApiService] Error de red Home: ${exception.message}")
            HomeResponse(success = false, message = "No fue posible obtener datos del usuario")
        }
    }

    suspend fun getBillingOverview(idUsuario: Int): BillingOverviewResponse {
        println("[AuthApiService] Consultando resumen de facturación para id_usuario=$idUsuario")
        return try {
            val response = client.get("$baseUrl/auth/billing-overview/$idUsuario")
            response.body()
        } catch (exception: ClientRequestException) {
            val errorBody = exception.response.body<BillingOverviewResponse>()
            println("[AuthApiService] Error resumen facturación: ${errorBody.message}")
            errorBody
        } catch (exception: HttpRequestTimeoutException) {
            println("[AuthApiService] Timeout resumen facturación")
            BillingOverviewResponse(success = false, message = "No fue posible cargar la facturación")
        } catch (exception: ResponseException) {
            println("[AuthApiService] Error HTTP resumen facturación: ${exception.message}")
            BillingOverviewResponse(success = false, message = "Error de servidor al cargar la facturación")
        } catch (exception: Exception) {
            println("[AuthApiService] Error de red resumen facturación: ${exception.message}")
            BillingOverviewResponse(success = false, message = "Error al cargar la información")
        }
    }

    suspend fun preparePayment(idUsuario: Int, idFactura: Int): PreparePaymentResponse {
        println("[AuthApiService] Preparando pago para id_usuario=$idUsuario id_factura=$idFactura")
        return try {
            val response = client.post("$baseUrl/auth/prepare-payment/$idUsuario") {
                contentType(ContentType.Application.Json)
                setBody(PreparePaymentRequest(id_factura = idFactura))
            }
            response.body()
        } catch (exception: ClientRequestException) {
            val errorBody = exception.response.body<PreparePaymentResponse>()
            println("[AuthApiService] Error preparando pago: ${errorBody.message}")
            errorBody
        } catch (exception: HttpRequestTimeoutException) {
            println("[AuthApiService] Timeout preparación de pago")
            PreparePaymentResponse(success = false, message = "No fue posible preparar el pago")
        } catch (exception: ResponseException) {
            println("[AuthApiService] Error HTTP preparación pago: ${exception.message}")
            PreparePaymentResponse(success = false, message = "Error de servidor al preparar el pago")
        } catch (exception: Exception) {
            println("[AuthApiService] Error de red preparación pago: ${exception.message}")
            PreparePaymentResponse(success = false, message = "No fue posible conectar con el servidor")
        }
    }
}
