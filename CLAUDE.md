# HeroScan — Proyecto Universitario Android

## Descripción
Aplicación Android para escanear códigos de barras de cómics físicos y obtener información relevante. Proyecto universitario individual, desarrollo progresivo durante el semestre.

## Tecnología
- Android Studio Quail 3 (2026.1.3)
- Kotlin
- Jetpack Compose (UI)
- Material Design 3
- CameraX (cámara funcionando, vista previa en vivo integrada en PantallaEscaneo)
- Google ML Kit Barcode Scanning (integrado, detecta códigos de barras en tiempo real desde la cámara)
- Retrofit + Gson (API de Metron para cómics y MyMemory para traducir descripciones)
- Coil (carga de imágenes desde URL/Uri)
- Vibración al encontrar un cómic (Vibrator; el vibrador es un actuador, no cuenta como sensor)
- Android Sensor Framework (sensores, pendiente de implementar)
- Navigation Compose (navegación entre pantallas)

## Arquitectura
MVVM sencilla según lo aprendido en clase:
```
UI / Compose → ViewModel → Repository → Retrofit / API
```
- La UI solo dibuja y reenvía eventos al ViewModel; no contiene lógica.
- Cada pantalla con lógica tiene su propio ViewModel, en el mismo paquete que la pantalla.
- La UI nunca usa DTOs de la API: el mapper los convierte a modelos de `model/`.

## Convención de nombres
- Todo el código (clases, funciones, variables, parámetros) se nombra en español.
- Excepciones: nombres de paquetes, sufijos técnicos (ViewModel, Repository, Api, Dto, UiState), métodos del framework (onCreate, onCleared) y campos de los DTO (deben coincidir con las claves del JSON).
- Cada función lleva un comentario `//` encima explicando qué hace.

## Estructura actual del proyecto
```
com.example.heroscan
├── MainActivity.kt                (aplica TemaHeroScan y muestra NavegacionHeroScan)
├── navigation/
│   ├── NavegacionHeroScan.kt      (NavHost con todas las pantallas)
│   └── Rutas.kt                   (constantes de rutas + convertir Comic a/desde JSON para la ruta de detalle)
├── model/                         (modelos de la app: Comic, Personaje, ComicResumen, TipoCodigo)
├── network/
│   ├── api/                       (MetronApi, TraduccionApi)
│   ├── dto/                       (DTOs que reflejan el JSON de las APIs)
│   ├── mapper/                    (MetronMapper: DTO → modelo)
│   └── ClienteRetrofit.kt
├── repository/
│   └── ComicRepository.kt         (busca por código, trae detalle, traduce descripción, obtiene personajes)
├── util/
│   ├── ClasificadorCodigo.kt      (limpiar y clasificar códigos: UPC-A, EAN-13, ISBN, ISSN)
│   └── Vibracion.kt               (vibrarDispositivo)
└── ui/
    ├── components/ComponentesComunes.kt (BarraSuperior, BotonAccionPrincipal, BotonCircular, Visor, PanelInferior, AreaEscaneo)
    ├── home/PantallaInicio.kt     (pantalla principal, pide permiso de cámara antes de escanear)
    ├── scan/
    │   ├── PantallaEscaneo.kt     (cámara en vivo con CameraX + código detectado)
    │   ├── EscaneoViewModel.kt    (ML Kit, linterna, código detectado)
    │   └── PantallaEscaneoPortada.kt (elegir imagen de la galería; la búsqueda por portada está pendiente)
    ├── search/
    │   ├── PantallaBusqueda.kt    (búsqueda por código escrito)
    │   ├── BusquedaViewModel.kt   (texto, filtro, búsqueda y selección de resultados)
    │   └── BusquedaUiState.kt
    ├── detail/PantallaDetalleComic.kt (detalle del cómic con datos reales de Metron)
    └── theme/                     (Colores, Tema, Tipografia: paleta oscura fija, sin modo claro ni dynamic color)

res/drawable/
└── imageback_scan_card.jpg  (imagen de fondo usada en PantallaInicio y en las pantallas de escaneo)
```

## Rutas de navegación (NavHost)
- `"inicio"` → PantallaInicio
- `"escaneo"` → PantallaEscaneo (cámara)
- `"escaneoPortada"` → PantallaEscaneoPortada
- `"busqueda"` → PantallaBusqueda
- `"detalleComic/{comicJson}"` → PantallaDetalleComic (el cómic viaja como JSON en la ruta)

## Decisiones CONFIRMADAS
- Aplicación Android nativa
- Kotlin + Jetpack Compose
- Desarrollo individual
- Uso de la cámara (CameraX)
- Escaneo de códigos de barras con Google ML Kit Barcode Scanning
- Consumo de una API externa (cuál API se decide después)
- Arquitectura sencilla basada en conceptos de clase
- Desarrollo progresivo durante el semestre
- Navigation Compose para navegación entre pantallas
- Paleta de colores oscura con acentos cian (#22E1FF) y magenta (#E23EDB)

## Decisiones PENDIENTES
- API definitiva para obtener información de cómics
- Cómo relacionar el código de barras con la información del cómic
- Sensor que se utilizará (acelerómetro, giroscopio o magnetómetro)
- Funcionalidad concreta del sensor
- Sistema de gamificación
- Necesidad de persistencia local
- Diseño visual de las demás pantallas
- Alcance definitivo del MVP

## Próximos pasos (en orden)
1. (hecho) Completar archivos pendientes (Comic.kt, MockComics.kt, ComicDetailScreen.kt)
2. (hecho) Diseñar la pantalla de escaneo (UI solamente)
3. (hecho) Implementar CameraX (cámara funcionando)
4. (hecho) Integrar ML Kit Barcode Scanning
5. (hecho) Mostrar código detectado en pantalla (se ve en el panel inferior de ScanScreen)
6. (hecho) Conectar API de cómics (Metron) y búsqueda por código escrito
7. (hecho) Reorganizar el código en MVVM con nombres en español

## Reglas de desarrollo
- NO adelantarse: implementar solo lo solicitado explícitamente.
- NO inventar requisitos ni funcionalidades adicionales.
- Priorizar simplicidad: la solución más sencilla que cumpla el objetivo.
- Explicar antes de implementar decisiones técnicas importantes.
- Código claro, sencillo, con nombres descriptivos.
- Explicar conceptos nuevos de Kotlin/Compose cuando aparezcan.
- No refactorizar sin razón concreta.
- No agregar librerías/dependencias innecesarias.
- No implementar arquitectura que aún no se necesite.
- Ante varias soluciones válidas: priorizar la más sencilla, comprensible y que use lo aprendido en clase.

## Requisitos de la asignatura
- Usar cámara o micrófono (se usa cámara).
- Usar al menos un sensor (acelerómetro, giroscopio o magnetómetro).
- Consumir una API externa.
- No puede ser un videojuego.
- Puede incluir gamificación (puntos, logros, colecciones, progreso).
- Analizar aplicaciones similares (competencia).

## Principio fundamental
> Si una funcionalidad no ayuda directamente al objetivo de HeroScan, probablemente no pertenece al proyecto.

## Comandos útiles
- Build: usar el botón Run de Android Studio o Shift+F10
- El proyecto usa Kotlin DSL para Gradle (build.gradle.kts)
- Dependencias se gestionan en libs.versions.toml

## Llamar al usuario por su nombre: "Mag"