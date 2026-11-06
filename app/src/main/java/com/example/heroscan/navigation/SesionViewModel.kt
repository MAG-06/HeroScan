package com.example.heroscan.navigation

import androidx.lifecycle.ViewModel
import com.example.heroscan.repository.CuentaRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

// Le dice a la navegación si hay una sesión iniciada, para decidir la primera pantalla (Acceso o Inicio).
// Así la UI no tiene que importar nada de repository: le pregunta a este ViewModel.
class SesionViewModel : ViewModel() {

    private val repositorio = CuentaRepository(FirebaseAuth.getInstance(), FirebaseFirestore.getInstance())

    // Devuelve true si hay una sesión guardada en el celular.
    // No hay parpadeo porque la respuesta es inmediata: Firebase guarda la sesión en el celular y la
    // tiene lista al abrir la app, sin esperar a internet. Por eso se puede decidir la primera pantalla
    // antes de dibujar nada, y nunca se ve Acceso "un instante" cuando ya hay sesión.
    fun haySesion(): Boolean {
        return repositorio.usuarioActual() != null
    }
}
