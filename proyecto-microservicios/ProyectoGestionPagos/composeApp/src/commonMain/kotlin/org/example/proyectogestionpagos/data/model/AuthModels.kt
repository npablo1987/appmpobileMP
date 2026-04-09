package org.example.proyectogestionpagos.data.model

import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest(
    val correo: String,
    val clave: String,
)

@Serializable
data class LoginUserData(
    val id_usuario: Int,
    val rut: String? = null,
    val nombres: String,
    val apellido_paterno: String,
    val apellido_materno: String? = null,
    val correo: String,
    val telefono: String? = null,
    val ciudad: String? = null,
    val estado_cuenta: String,
)

@Serializable
data class LoginResponse(
    val success: Boolean,
    val message: String,
    val data: LoginUserData? = null,
)

@Serializable
data class HomeUserData(
    val id_usuario: Int,
    val rut: String? = null,
    val nombre_completo: String,
    val correo: String,
    val telefono: String? = null,
    val direccion: String? = null,
    val ciudad: String? = null,
    val estado_cuenta: String,
    val fecha_registro: String,
)

@Serializable
data class HomeResponse(
    val success: Boolean,
    val message: String,
    val data: HomeUserData? = null,
)
