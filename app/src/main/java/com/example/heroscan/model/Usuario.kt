package com.example.heroscan.model

// Datos básicos de la cuenta con la sesión iniciada: su identificador en Firebase y su correo.
// La contraseña nunca se guarda aquí.
data class Usuario(
    val uid: String,
    val correo: String
)
