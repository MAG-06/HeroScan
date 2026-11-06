package com.example.heroscan.ui.perfil

import androidx.lifecycle.ViewModel
import com.example.heroscan.repository.CuentaRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

// ViewModel de Perfil: da el correo de la cuenta con sesión y cierra la sesión.
// La pantalla no habla con el repositorio: le pide todo a este ViewModel.
class PerfilViewModel : ViewModel() {

    private val repositorio = CuentaRepository(FirebaseAuth.getInstance(), FirebaseFirestore.getInstance())

    // Correo de la cuenta con sesión (vacío si por algún motivo no hay sesión).
    // Se lee una sola vez al crear el ViewModel: mientras Perfil está abierto la cuenta no cambia.
    val correo: String = repositorio.usuarioActual()?.correo ?: ""

    // Cierra la sesión en este celular. La pantalla navega a Acceso después de llamarla.
    fun cerrarSesion() {
        repositorio.cerrarSesion()
    }
}
