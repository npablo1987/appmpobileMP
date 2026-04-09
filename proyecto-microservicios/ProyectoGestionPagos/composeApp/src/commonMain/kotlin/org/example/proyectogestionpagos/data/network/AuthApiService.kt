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
import org.example.proyectogestionpagos.data.model.LoginRequest
import org.example.proyectogestionpagos.data.model.LoginResponse

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
}
