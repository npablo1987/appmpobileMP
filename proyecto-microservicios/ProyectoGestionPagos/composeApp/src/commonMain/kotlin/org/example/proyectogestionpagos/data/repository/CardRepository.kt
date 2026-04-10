package org.example.proyectogestionpagos.data.repository

import org.example.proyectogestionpagos.data.model.GuardarTarjetaResponse
import org.example.proyectogestionpagos.data.model.PagoConTarjetaGuardadaResponse
import org.example.proyectogestionpagos.data.model.TarjetaDeleteResponse
import org.example.proyectogestionpagos.data.model.TarjetasListResponse
import org.example.proyectogestionpagos.data.network.CardApiService

class CardRepository {
    private val cardApiService = CardApiService()

    suspend fun guardarTarjeta(idUsuario: Int, token: String, email: String): GuardarTarjetaResponse {
        return cardApiService.guardarTarjeta(idUsuario, token, email)
    }

    suspend fun listarTarjetas(idUsuario: Int): TarjetasListResponse? {
        return cardApiService.listarTarjetas(idUsuario)
    }

    suspend fun marcarTarjetaDefault(idTarjeta: Int, isDefault: Boolean): GuardarTarjetaResponse? {
        return cardApiService.marcarTarjetaDefault(idTarjeta, isDefault)
    }

    suspend fun eliminarTarjeta(idTarjeta: Int, idUsuario: Int): TarjetaDeleteResponse? {
        return cardApiService.eliminarTarjeta(idTarjeta, idUsuario)
    }

    suspend fun pagarConTarjetaGuardada(
        idUsuario: Int,
        idTarjeta: Int,
        descripcion: String,
        monto: Double
    ): PagoConTarjetaGuardadaResponse {
        return cardApiService.pagarConTarjetaGuardada(idUsuario, idTarjeta, descripcion, monto)
    }
}
