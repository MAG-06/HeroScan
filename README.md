# HeroScan

App Android para coleccionistas de cómics: escaneas el código de barras de un cómic físico con la cámara y la app te muestra su información (título, número, editorial, fecha, portada, descripción, créditos y personajes).

Proyecto universitario individual. Se construye por fases a lo largo del semestre, a medida que se ven nuevos temas en clase.

**Autor:** Mateo Albornoz Gil

---

## Funcionalidades

- **Escanear con la cámara:** CameraX + Google ML Kit detectan códigos UPC-A y EAN-13 en tiempo real. Si el código corresponde a un solo cómic se abre su detalle; si corresponde a varios (por ejemplo, toda la serie), se muestra la lista para elegir. Incluye linterna.
- **Buscar por texto:** filtros TODO, TÍTULO, PERSONAJE y CÓDIGO (UPC de 12 o 17 dígitos).
- **Detalle del cómic:** portada, datos de la edición, descripción traducida al español, créditos y hasta 5 personajes con imagen.
- **Buscar por portada:** permite elegir una imagen de la galería (la identificación todavía está en desarrollo).
- Vibración al encontrar un cómic y manejo de permisos de cámara y galería.

## Tecnologías

| Área | Tecnología |
|---|---|
| Lenguaje y UI | Kotlin, Jetpack Compose, Material Design 3 |
| Navegación | Navigation Compose |
| Cámara y escaneo | CameraX, Google ML Kit Barcode Scanning |
| Red | Retrofit, Gson, OkHttp |
| Imágenes | Coil |
| APIs externas | [Metron](https://metron.cloud) (información de cómics), [MyMemory](https://mymemory.translated.net) (traducción) |

## Arquitectura

MVVM sencilla:

```
UI (Compose) → ViewModel → Repository → Retrofit / API
```

- Cada pantalla con lógica tiene su propio ViewModel (`EscaneoViewModel`, `BusquedaViewModel`).
- `ComicRepository` recibe las APIs por constructor y los ViewModels se las pasan.
- Los estados de cada pantalla se manejan con `sealed interface` (cargando, encontrado, varios resultados, error).
- Entre pantallas los datos viajan por la ruta de navegación.

```
com.example.heroscan
├── navigation/   NavHost y rutas
├── model/        modelos de la app (Comic, Personaje, ComicResumen, TipoCodigo)
├── network/      Retrofit, APIs, DTOs y mapper
├── repository/   ComicRepository
├── util/         clasificador de códigos de barras y vibración
└── ui/           pantallas (inicio, escaneo, búsqueda, detalle), componentes comunes y tema
```

## Cómo correr el proyecto

1. Clonar el repositorio y abrirlo en Android Studio.
2. Conseguir un token de la API de Metron (cuenta gratuita en [metron.cloud](https://metron.cloud)).
3. En el archivo `local.properties` de la raíz del proyecto (lo crea Android Studio), agregar al final:
   ```
   METRON_TOKEN=tu_token
   ```
   Este archivo no se sube a git, así que el token nunca queda en el repositorio.
4. **File → Sync Project with Gradle Files**.
5. Ejecutar en un celular o emulador (Android 7.0 / API 24 o superior).

> Si las búsquedas muestran **HTTP 401**, falta el token o está mal escrito en `local.properties`.

## Limitaciones conocidas

- El escáner no lee el suplemento de 5 dígitos (EAN-5) de los cómics, porque ML Kit no lo soporta. Por eso al escanear se encuentra la **serie** y se elige el número de una lista. Para un número exacto se pueden escribir los 17 dígitos en la búsqueda.
- Solo se soportan códigos UPC-A y EAN-13; los ISBN (tomos y novelas gráficas) todavía no.
- Las listas muestran la primera página de resultados de Metron.
- La búsqueda por personaje usa el primer personaje que coincide con el nombre.
- Las descripciones de más de 500 caracteres se muestran en inglés (límite de MyMemory).

## Próximas fases

- Uso de un sensor del teléfono (acelerómetro) para una interacción dentro de la app.
- Identificación del cómic a partir de la foto de la portada.
- Funciones de colección personal.
