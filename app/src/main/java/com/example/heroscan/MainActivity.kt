package com.example.heroscan

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.example.heroscan.navigation.NavegacionHeroScan
import com.example.heroscan.ui.theme.TemaHeroScan

// Punto de entrada de la app: aplica el tema y muestra la navegación entre pantallas.
class MainActivity : ComponentActivity() {

    // Se ejecuta al abrir la app: activa la pantalla completa (edge to edge) y dibuja la interfaz con Compose.
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TemaHeroScan {
                Scaffold(modifier = Modifier.fillMaxSize()) { espacioInterno ->
                    NavegacionHeroScan(modifier = Modifier.padding(espacioInterno))
                }
            }
        }
    }
}
