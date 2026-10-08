package com.example.firstapp.models

import com.google.firebase.Timestamp

/**
 * Modelo de datos para representar el perfil completo del usuario en Cloud Firestore.
 * Esta clase se vincula 1:1 con el UID generado por Firebase Authentication.
 */
data class Usuario(
    val uid: String = "",
    val correo: String = "",
    val nombreCompleto: String = "",
    val rut: String = "",
    val telefono: String = "",
    val rol: String = "usuario", // Valores permitidos: "usuario", "tecnico", "administrador"
    val fechaRegistro: Timestamp? = null,
    val activo: Boolean = true,
    val preferencias: Map<String, Any> = mapOf(
        "notificaciones" to true,
        "idioma" to "Español",
        "unidadTemperatura" to "Celsius"
    )
) {
    /**
     * Convierte el modelo a un Map compatible con Firestore.
     */
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "uid" to uid,
            "correo" to correo,
            "nombreCompleto" to nombreCompleto,
            "rut" to rut,
            "telefono" to telefono,
            "rol" to rol,
            "fechaRegistro" to (fechaRegistro ?: Timestamp.now()),
            "activo" to activo,
            "preferencias" to preferencias
        )
    }
}