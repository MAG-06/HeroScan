# HeroScan

App Android para coleccionistas de cómics: escaneas el código de barras de un cómic físico con la cámara (o eliges una foto de su portada) y la app te muestra su información (título, número, editorial, fecha, portada, descripción, créditos y personajes).

Proyecto universitario individual. Se construye por fases a lo largo del semestre, a medida que se ven nuevos temas en clase.

**Autor:** Mateo Albornoz Gil

---

## Funcionalidades

- **Escanear con la cámara:** CameraX + Google ML Kit detectan códigos UPC-A y EAN-13 en tiempo real. Si el código corresponde a un solo cómic se abre su detalle; si corresponde a varios (por ejemplo, toda la serie), se muestra la lista para elegir. Incluye linterna.
- **Agitar para escanear:** en la pantalla de inicio, agitar el teléfono abre el escáner (acelerómetro).
- **Buscar por portada:** eliges una imagen de la galería y la IA de Gemini identifica el título y el número del cómic (también en ediciones en español, buscando la edición original en inglés). Luego la app lo busca en Metron: si encuentra uno abre su detalle; si encuentra varios abre la búsqueda por título.
- **Buscar por texto:** filtros TODO, TÍTULO, PERSONAJE y CÓDIGO (12, 13 o 17 dígitos).
- **Detalle del cómic:** portada, datos de la edición, descripción traducida al español, créditos y hasta 5 personajes con imagen.
- Vibración al encontrar un cómic con el escáner y manejo de permisos de cámara y galería.

## Tecnologías

| Área | Tecnología |
|---|---|
| Lenguaje y UI | Kotlin, Jetpack Compose, Material Design 3 |
| Navegación | Navigation Compose |
| Cámara y escaneo | CameraX, Google ML Kit Barcode Scanning |
| Sensores | Acelerómetro (SensorManager) |
| Red | Retrofit, Gson, OkHttp |
| Imágenes | Coil |
| APIs externas | [Metron](https://metron.cloud) (información de cómics), [MyMemory](https://mymemory.translated.net) (traducción), [Gemini](https://ai.google.dev) (reconocimiento de portadas) |

## Arquitectura

MVVM sencilla:

```
UI (Compose) → ViewModel → Repository → Retrofit / API
```

- Cada pantalla con lógica tiene su propio ViewModel (`EscaneoViewModel`, `BusquedaViewModel`, `PortadaViewModel`).
- `ComicRepository` recibe las APIs (Metron, MyMemory y Gemini) por constructor y los ViewModels se las pasan.
- Los estados de cada pantalla se manejan con `sealed interface` (cargando, encontrado, varios resultados, error).
- Entre pantallas los datos viajan por la ruta de navegación.

```
com.example.heroscan
├── navigation/   NavHost y rutas
├── model/        modelos de la app (Comic, Personaje, ComicResumen, TipoCodigo)
├── network/      Retrofit, APIs, DTOs y mapper
├── repository/   ComicRepository
├── util/         clasificador de códigos de barras, vibración, sensores e imágenes
└── ui/           pantallas (inicio, escaneo, portada, búsqueda, detalle), componentes comunes y tema
```

## Cómo correr el proyecto

1. Clonar el repositorio y abrirlo en Android Studio.
2. Conseguir un token de la API de Metron (cuenta gratuita en [metron.cloud](https://metron.cloud)).
3. Para buscar por portada, conseguir una clave gratuita de Gemini en [Google AI Studio](https://aistudio.google.com/apikey) (**Get API key**).
4. En el archivo `local.properties` de la raíz del proyecto (lo crea Android Studio), agregar al final:
   ```
   METRON_TOKEN=tu_token
   GEMINI_API_KEY=tu_clave
   ```
   Opcional: `GEMINI_MODEL=nombre_del_modelo` (por defecto `gemini-3.5-flash-lite`).
   Este archivo no se sube a git, así que el token y la clave nunca quedan en el repositorio.
5. **File → Sync Project with Gradle Files**.
6. Ejecutar en un celular o emulador (Android 7.0 / API 24 o superior).

> Si las búsquedas muestran **HTTP 401**, falta el token de Metron o está mal escrito en `local.properties`.
> Si "Buscar por portada" muestra **"Falta configurar la clave de Gemini"**, falta `GEMINI_API_KEY`. El resto de la app funciona sin ella.

## Limitaciones conocidas

- El escáner no lee el suplemento de 5 dígitos (EAN-5) de los cómics, porque ML Kit no lo soporta. Por eso al escanear se encuentra la **serie** y se elige el número de una lista. Para un número exacto se pueden escribir los 17 dígitos en la búsqueda.
- Solo se soportan códigos UPC-A y EAN-13; los ISBN (tomos y novelas gráficas) todavía no.
- Buscar por portada usa la capa gratuita de Gemini: tiene límites de uso y el servicio puede estar saturado a ratos. Si la portada da varios resultados, la lista se busca solo por título. Los cómics que no están en Metron (por ejemplo, tomos recopilatorios) no se encuentran.
- Las listas muestran la primera página de resultados de Metron.
- La búsqueda por personaje usa el primer personaje que coincide con el nombre.
- Las descripciones de más de 500 caracteres se muestran en inglés (límite de MyMemory).

## Próximas fases

- Soporte para ISBN y códigos de ediciones en español.
- Funciones de colección personal.
