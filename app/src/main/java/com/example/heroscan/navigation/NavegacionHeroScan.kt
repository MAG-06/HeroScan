package com.example.heroscan.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.heroscan.ui.detail.PantallaDetalleComic
import com.example.heroscan.ui.home.PantallaInicio
import com.example.heroscan.ui.scan.PantallaEscaneo
import com.example.heroscan.ui.scan.PantallaEscaneoPortada
import com.example.heroscan.ui.search.PantallaBusqueda

// Define el NavHost con todas las pantallas de la app y a dónde lleva cada botón.
@Composable
fun NavegacionHeroScan(modifier: Modifier = Modifier) {

    val controladorNavegacion: NavHostController = rememberNavController()

    NavHost(navController = controladorNavegacion, startDestination = Rutas.INICIO, modifier = modifier) {

        composable(Rutas.INICIO) {
            PantallaInicio(
                alEscanear = { controladorNavegacion.navigate(Rutas.ESCANEO) },
                alBuscarPorPortada = { controladorNavegacion.navigate(Rutas.ESCANEO_PORTADA) },
                alBuscarPorTexto = { controladorNavegacion.navigate(Rutas.BUSQUEDA) }
            )
        }

        composable(Rutas.ESCANEO) {
            PantallaEscaneo(alVolver = { controladorNavegacion.popBackStack() })
        }

        composable(Rutas.ESCANEO_PORTADA) {
            PantallaEscaneoPortada(alVolver = { controladorNavegacion.popBackStack() })
        }

        composable(Rutas.BUSQUEDA) {
            PantallaBusqueda(
                alVolver = { controladorNavegacion.popBackStack() },
                alEncontrarComic = { comic ->
                    controladorNavegacion.navigate(crearRutaDetalleComic(comic))
                }
            )
        }

        composable(Rutas.DETALLE_COMIC) { entradaNavegacion ->
            val comicJson = entradaNavegacion.arguments?.getString(Rutas.ARGUMENTO_COMIC) ?: ""

            PantallaDetalleComic(
                comic = obtenerComicDeRuta(comicJson),
                alVolver = { controladorNavegacion.popBackStack() }
            )
        }
    }
}
