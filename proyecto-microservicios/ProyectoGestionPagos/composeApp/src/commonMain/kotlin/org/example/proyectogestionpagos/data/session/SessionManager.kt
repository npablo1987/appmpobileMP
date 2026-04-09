package org.example.proyectogestionpagos.data.session

import org.example.proyectogestionpagos.data.model.LoginUserData
import org.example.proyectogestionpagos.data.model.BillingOverviewData

object SessionManager {
    var idUsuario: Int? = null
        private set

    var loginUser: LoginUserData? = null
        private set

    var billingOverview: BillingOverviewData? = null
        private set

    fun saveSession(userData: LoginUserData) {
        println("[SessionManager] Guardando sesión para id_usuario=${userData.id_usuario}")
        idUsuario = userData.id_usuario
        loginUser = userData
    }

    fun clearSession() {
        println("[SessionManager] Limpiando sesión")
        idUsuario = null
        loginUser = null
        billingOverview = null
    }

    fun saveBillingOverview(data: BillingOverviewData) {
        println("[SessionManager] Guardando resumen de facturación")
        billingOverview = data
    }
}
