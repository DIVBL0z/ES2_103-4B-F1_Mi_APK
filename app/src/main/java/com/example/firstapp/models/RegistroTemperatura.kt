package com.example.firstapp.models

import com.google.firebase.firestore.DocumentId

const val COLECCION = "registros_temperatura"

data class RegistroTemperatura(
    @DocumentId val id: String? = null,
    val ubicacion: String = "",
    val temperatura: Double = 0.0,
    val hora: String = ""
)