package com.example.heroscan.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.heroscan.ui.acceso.PantallaAcceso
import com.example.heroscan.ui.detail.PantallaDetalleComic
import com.example.heroscan.ui.home.PantallaInicio
import com.example.heroscan.ui.perfil.PantallaPerfil
import com.example.heroscan.ui.scan.PantallaEscaneo
import com.example.heroscan.ui.scan.PantallaEscaneoPortada
import com.example.heroscan.ui.search.PantallaBusqueda

// Define el NavHost con todas las pantallas de la app y a dónde lleva cada botón.
// Cada pantalla crea su propio ViewModel; entre pantallas solo viajan datos por la ruta.
@Composable
fun NavegacionHeroScan(
    modifier: Modifier = Modifier,
    sesionViewModel: SesionViewModel = viewModel()
) {

    val controladorNavegacion: NavHostController = rememberNavController()

    // Primera pantalla: Inicio si ya hay sesión, Acceso si no. remember hace que se decida una sola vez
    // (no en cada redibujo). Como haySesion() responde al instante, no se ve Acceso antes de Inicio.
    val rutaInicial = remember {
        if (sesionViewModel.haySesion()) Rutas.INICIO else Rutas.ACCESO
    }

    NavHost(navController = controladorNavegacion, startDestination = rutaInicial, modifier = modifier) {

        // Acceso: al entrar (iniciar sesión o crear cuenta) se va a Inicio y Acceso sale de la pila.
        // popUpTo(Rutas.ACCESO) { inclusive = true } saca de la pila todo hasta Acceso, incluido Acceso.
        // Así Inicio queda como la base de la pila: "atrás" desde Inicio cierra la app y no vuelve a Acceso.
        composable(Rutas.ACCESO) {
            PantallaAcceso(
                alIniciarSesion = {
                    controladorNavegacion.navigate(Rutas.INICIO) {
                        popUpTo(Rutas.ACCESO) { inclusive = true }
                    }
                }
            )
        }

        // Inicio: ESCANEAR, BÚSQUEDA y PERFIL son acciones de la barra, así que van con navegarDesdeBarra.
        // Como Inicio es la base de la pila, desde aquí equivale a un navigate normal: la pila queda [Inicio, destino].
        // La tarjeta de escaneo, la sacudida y "Buscar por texto" usan las mismas acciones que la barra.
        composable(Rutas.INICIO) {
            PantallaInicio(
                alEscanear = { navegarDesdeBarra(controladorNavegacion, Rutas.ESCANEO) },
                alBuscarPorPortada = { controladorNavegacion.navigate(Rutas.ESCANEO_PORTADA) },
                alBuscarPorTexto = { navegarDesdeBarra(controladorNavegacion, crearRutaBusqueda()) },
                alIrAPerfil = { navegarDesdeBarra(controladorNavegacion, Rutas.PERFIL) }
            )
        }

        // Perfil: la pila es [Inicio, Perfil], así que "atrás" vuelve a Inicio.
        composable(Rutas.PERFIL) {
            PantallaPerfil(
                // Cerrar sesión: popUpTo(graph.id) { inclusive = true } vacía toda la pila (Inicio y Perfil)
                // y deja solo Acceso, así "atrás" desde Acceso cierra la app y no vuelve a Perfil.
                // clearBackStack borra además lo que la barra pudo dejar guardado (saveState) de Perfil e Inicio,
                // para que la próxima cuenta no vea el Perfil (ni el correo) de la anterior.
                alCerrarSesion = {
                    controladorNavegacion.navigate(Rutas.ACCESO) {
                        popUpTo(controladorNavegacion.graph.id) { inclusive = true }
                    }
                    controladorNavegacion.clearBackStack(Rutas.PERFIL)
                    controladorNavegacion.clearBackStack(Rutas.INICIO)
                },
                alIrAInicio = { navegarDesdeBarra(controladorNavegacion, Rutas.INICIO) },
                alEscanear = { navegarDesdeBarra(controladorNavegacion, Rutas.ESCANEO) },
                alBuscar = { navegarDesdeBarra(controladorNavegacion, crearRutaBusqueda()) }
            )
        }

        composable(Rutas.ESCANEO) {
            PantallaEscaneo(
                alVolver = { controladorNavegacion.popBackStack() },
                alEncontrarComic = { comic ->
                    controladorNavegacion.navigate(crearRutaDetalleComic(comic))
                },
                alVariosResultados = { codigo ->
                    controladorNavegacion.navigate(crearRutaBusqueda(codigo))
                }
            )
        }

        composable(Rutas.ESCANEO_PORTADA) {
            PantallaEscaneoPortada(
                alVolver = { controladorNavegacion.popBackStack() },
                alEncontrarComic = { comic ->
                    controladorNavegacion.navigate(crearRutaDetalleComic(comic))
                },
                alVariosResultados = { titulo ->
                    controladorNavegacion.navigate(crearRutaBusqueda(titulo = titulo))
                }
            )
        }

        // Los argumentos "codigo" y "titulo" son opcionales: si no vienen, valen null y la búsqueda se abre vacía
        composable(
            route = Rutas.BUSQUEDA,
            arguments = listOf(
                navArgument(Rutas.ARGUMENTO_CODIGO) {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                },
                navArgument(Rutas.ARGUMENTO_TITULO) {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) {
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

// Navega a una pestaña desde la barra inferior (la usan Inicio y Perfil). Así la pila nunca crece al saltar entre pestañas:
// - popUpTo(Rutas.INICIO) saca todo lo que está encima de Inicio (Inicio se queda) antes de ir al destino,
//   así "atrás" desde cualquier pestaña vuelve a Inicio. saveState = true guarda el estado de lo que se saca.
//   Se usa Rutas.INICIO fijo y no el inicio del grafo, porque el inicio del grafo puede ser Acceso.
// - launchSingleTop = true evita poner dos veces la misma pantalla si ya está arriba (por ejemplo, Inicio desde Perfil).
// - restoreState = true recupera ese estado guardado al volver a la pestaña.
private fun navegarDesdeBarra(controlador: NavHostController, ruta: String) {
    controlador.navigate(ruta) {
        popUpTo(Rutas.INICIO) {
            saveState = true
        }
        launchSingleTop = true
        restoreState = true
    }
}
