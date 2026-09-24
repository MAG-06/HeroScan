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
- Cada pantalla con lógica tiene su propio ViewModel, en el mismo paquete que la pantalla. Los ViewModels NO se comparten entre pantallas: los datos viajan por la ruta de navegación.
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
    │   ├── PantallaEscaneo.kt     (cámara en vivo con CameraX; navega al detalle o a la búsqueda según el resultado)
    │   ├── EscaneoViewModel.kt    (ML Kit, linterna, y busca el cómic en cuanto detecta un código)
    │   ├── EscaneoUiState.kt      (Escaneando, Buscando, Encontrado, VariosResultados, Error)
    │   └── PantallaEscaneoPortada.kt (elegir imagen de la galería; la búsqueda por portada está pendiente)
    ├── search/
    │   ├── PantallaBusqueda.kt    (búsqueda por código, título o personaje)
    │   ├── BusquedaViewModel.kt   (texto, filtro, búsqueda y selección; lee el código opcional de la ruta con SavedStateHandle)
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
- `"busqueda?codigo={codigo}"` → PantallaBusqueda (el código es opcional; el escáner lo manda cuando hay varios resultados)
- `"detalleComic/{comicJson}"` → PantallaDetalleComic (el cómic viaja como JSON en la ruta)

## Limitaciones conocidas
- **El escáner no lee el suplemento de 5 dígitos (EAN-5)** de los cómics. ML Kit no soporta suplementos EAN-2/EAN-5
  (no hay formato ni opción para activarlos; solicitud abierta desde 2020: github.com/googlesamples/mlkit/issues/184).
  Confirmado con prueba real el 2026-09-23: `rawValue` y `displayValue` devuelven solo los 12 dígitos del UPC-A.
  Consecuencia: al escanear se encuentra la **serie** (lista de números para elegir), no el número exacto.
  Para el número exacto, el usuario debe escribir los 17 dígitos (UPC + suplemento) en la búsqueda.
  Cambiar a ZXing permitiría leerlo, pero se descartó para no complicar el proyecto.
- **Solo se buscan códigos UPC-A y EAN-13.** Los ISBN (tomos, novelas gráficas) muestran "Tipo de código no soportado aún".
- **Las listas muestran solo la primera página de resultados de Metron** (la API responde paginado y la app no pide las páginas siguientes).
- **La búsqueda por personaje usa el primer personaje que coincida** con el nombre escrito.
- **Las descripciones de más de 500 caracteres no se traducen** (límite de MyMemory); si la traducción falla, se muestra el texto original en inglés.
- **El detalle muestra como máximo 5 personajes.**
- **Buscar por portada:** solo permite elegir la imagen; el botón ENVIAR todavía no hace nada.
- **Barra inferior de PantallaInicio:** el botón BÚSQUEDA todavía no navega a ninguna parte.

### Posible solución futura: inteligencia artificial
Si en el futuro se integra una IA en la app, se estima que estas limitaciones se resolverían en un **70%** aproximadamente. Por ejemplo:
- Reconocer el cómic a partir de una foto de la portada (completaría "Buscar por portada").
- Leer los dígitos del suplemento desde la imagen, sin depender de ML Kit, para encontrar el número exacto al escanear.
- Traducir descripciones largas sin el límite de MyMemory.
- Interpretar búsquedas ambiguas (por ejemplo, elegir el personaje correcto entre varios con nombre parecido).

Las limitaciones que dependen de cómo está programada la app (paginación, máximo de personajes, botones pendientes)
se resuelven con código normal, no con IA.

## Decisiones CONFIRMADAS
- Aplicación Android nativa
- Kotlin + Jetpack Compose
- Desarrollo individual
- Uso de la cámara (CameraX)
- Escaneo de códigos de barras con Google ML Kit Barcode Scanning
- API externa definitiva: **Metron** (metron.cloud) para la información de cómics, con token de autenticación
- API de traducción: **MyMemory** para traducir las descripciones al español
- Relación código de barras ↔ cómic: el UPC se busca en Metron. Con 17 dígitos (UPC + suplemento) se busca el número exacto (`upc`);
  con 12 dígitos se buscan todos los números de la serie (`upc_starts_with`). Los EAN-13 se recortan a 12 dígitos.
- Búsqueda por texto en Metron: por código, por título de serie y por personaje (filtros TODO, TÍTULO, PERSONAJE, CÓDIGO)
- No se usará Google Books
- Arquitectura sencilla basada en conceptos de clase
- Desarrollo progresivo durante el semestre
- Navigation Compose para navegación entre pantallas
- Paleta de colores oscura con acentos cian (#22E1FF) y magenta (#E23EDB)

## Decisiones PENDIENTES
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
5. (hecho) Mostrar código detectado en pantalla (luego reemplazado: al detectar un código, PantallaEscaneo busca el cómic automáticamente)
6. (hecho) Conectar API de cómics (Metron) y búsqueda por código, título y personaje
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