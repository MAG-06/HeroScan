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
- Vibración al encontrar un cómic con el escáner y manejo del permiso de cámara.
- **Inicio de sesión obligatorio:** con correo y contraseña (Firebase). Al crear la cuenta se elige un nombre de usuario único y un medidor muestra qué tan segura es la contraseña. Incluye recuperar la contraseña por correo y un límite de intentos fallidos.
- **Perfil:** pestaña con el correo de la cuenta y el botón para cerrar sesión (con confirmación).

## Tecnologías

| Área | Tecnología |
|---|---|
| Lenguaje y UI | Kotlin, Jetpack Compose, Material Design 3 |
| Navegación | Navigation Compose con rutas tipadas (`@Serializable` + kotlinx-serialization) |
| Cámara y escaneo | CameraX, Google ML Kit Barcode Scanning |
| Sensores | Acelerómetro (SensorManager) |
| Red | Retrofit, Gson, OkHttp |
| Imágenes | Coil |
| Cuentas y datos | Firebase Authentication, Cloud Firestore (Firebase BoM 34.19.0) |
| APIs externas | [Metron](https://metron.cloud) (información de cómics), [MyMemory](https://mymemory.translated.net) (traducción), [Gemini](https://ai.google.dev) (reconocimiento de portadas) |

## Arquitectura

MVVM sencilla:

```
UI (Compose) → ViewModel → Repository → Retrofit / API
```

- Cada pantalla con lógica tiene su propio ViewModel (`EscaneoViewModel`, `BusquedaViewModel`, `PortadaViewModel`).
- `AccesoViewModel` maneja iniciar sesión, crear cuenta y recuperar la contraseña; `PerfilViewModel` da el correo de la cuenta y cierra la sesión; `SesionViewModel` decide si la app abre en Acceso o en Inicio.
- `ComicRepository` recibe las APIs (Metron, MyMemory y Gemini) por constructor y los ViewModels se las pasan.
- `CuentaRepository` usa Firebase Authentication y Firestore para crear la cuenta, iniciar y cerrar sesión y recuperar la contraseña.
- Los estados de cada pantalla se manejan con `sealed interface` (cargando, encontrado, varios resultados, error).
- Entre pantallas solo viaja por la ruta lo necesario (por ejemplo, el id del cómic); la pantalla de destino busca sus datos con su propio ViewModel.

```
com.example.heroscan
├── navigation/   NavHost, rutas y SesionViewModel
├── model/        modelos de la app (Comic, Personaje, ComicResumen, TipoCodigo, Usuario)
├── network/      Retrofit, APIs, DTOs y mapper
├── repository/   ComicRepository y CuentaRepository
├── util/         clasificador de códigos de barras, vibración, sensores, imágenes y, para las cuentas, ValidacionCuenta, EvaluadorClave y LimitadorIntentos
└── ui/           pantallas (acceso, inicio, escaneo, portada, búsqueda, detalle, perfil), componentes comunes y tema
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

> Si las búsquedas muestran **"Falta configurar el token de Metron o no es válido"**, falta `METRON_TOKEN` o está mal escrito en `local.properties`.
> Si "Buscar por portada" muestra **"Falta configurar la clave de Gemini"**, falta `GEMINI_API_KEY`. El resto de la app funciona sin ella.

## Configurar Firebase (inicio de sesión)

La app usa Firebase para las cuentas. El archivo `app/google-services.json` no está en el repositorio, así que cada persona que clone el proyecto tiene que crear su propio proyecto de Firebase. Hazlo antes del paso 5 de "Cómo correr el proyecto".

1. Entrar a [console.firebase.google.com](https://console.firebase.google.com) y crear un proyecto nuevo. Alcanza con el plan **Spark**, que es gratis. Google Analytics no hace falta.
2. En el proyecto, **agregar una app Android** con el nombre de paquete `com.example.heroscan`. El apodo y el SHA-1 son opcionales. Descargar el `google-services.json` y ponerlo en la carpeta `app/`, junto a `app/build.gradle.kts`. Ese archivo está en `.gitignore`: **nunca lo subas al repositorio**, porque tiene los datos de tu proyecto.
3. **Authentication → Comenzar → Sign-in method** → habilitar **Correo electrónico/contraseña**. Solo hace falta el primer interruptor, no el de "vínculo de correo electrónico".
   Recomendado:
   - En **Settings**, activar la **protección contra la enumeración de correos electrónicos**.
   - En **Settings**, en **Política de contraseñas**, poner un mínimo de **8 caracteres** con **Require enforcement**.
   - En **Templates**, poner el idioma de las plantillas en **español**, para que el correo de recuperar contraseña llegue en español.
4. **Firestore Database → Crear base de datos**. La ubicación se elige al crearla y **no se puede cambiar después**; este proyecto usa `southamerica-east1`. Después, en la pestaña **Rules**, pegar estas reglas y tocar **Publicar**:
   ```
   rules_version = '2';
   service cloud.firestore {
     match /databases/{database}/documents {
       match /usuarios/{uid} {
         allow read, write: if request.auth != null && request.auth.uid == uid;
       }
       match /nombresUsuario/{nombre} {
         allow get: if true;
         allow create: if request.auth != null
                       && request.resource.data.uid == request.auth.uid;
         allow update, delete: if false;
       }
     }
   }
   ```
   Con estas reglas, cada usuario solo puede leer y escribir su propio perfil. Además, un nombre de usuario se puede reservar una sola vez y no se puede cambiar ni borrar.
5. **File → Sync Project with Gradle Files** y ejecutar la app. La primera pantalla es la de inicio de sesión.

Qué se guarda en Firestore:
- `usuarios/{uid}`: `nombreUsuario`, `correo`, `codigos` (lista) y `fechaRegistro`.
- `nombresUsuario/{nombre}`: `{ uid }`. Sirve para que dos cuentas no tengan el mismo nombre de usuario.
- **La contraseña no se guarda en Firestore:** la maneja Firebase Authentication.

> Si el build falla con **"File google-services.json is missing. The Google Services Plugin cannot function without it."**, falta el archivo en `app/`. Descárgalo de la consola de Firebase: **Configuración del proyecto → Tus apps → google-services.json**. Ponlo en `app/` y vuelve a sincronizar Gradle.
> Si dice **"No matching client found for package name 'com.example.heroscan'"**, la app de Firebase se registró con otro nombre de paquete. Agrega una app Android con `com.example.heroscan` y descarga el archivo de nuevo.
> Si al crear la cuenta aparece **"No se pudo completar la operación. Inténtalo de nuevo."** aunque haya internet, revisa que la base de Firestore esté creada y que las reglas estén publicadas tal cual.
> Si el correo para recuperar la contraseña no llega, revisa la carpeta de spam: Firebase lo envía desde una dirección genérica (noreply@…firebaseapp.com) y Gmail suele marcarlo como spam.

## Limitaciones conocidas

- El escáner no lee el suplemento de 5 dígitos (EAN-5) de los cómics, porque ML Kit no lo soporta. Por eso al escanear se encuentra la **serie** y se elige el número de una lista. Para un número exacto se pueden escribir los 17 dígitos en la búsqueda.
- Solo se soportan códigos UPC-A y EAN-13; los ISBN (tomos y novelas gráficas) todavía no.
- Buscar por portada usa la capa gratuita de Gemini: tiene límites de uso y el servicio puede estar saturado a ratos. Si la portada da varios resultados, la lista se busca solo por título. Los cómics que no están en Metron (por ejemplo, tomos recopilatorios) no se encuentran.
- Las listas muestran la primera página de resultados de Metron.
- La búsqueda por personaje usa el primer personaje que coincide con el nombre.
- Las descripciones de más de 500 caracteres se muestran en inglés (límite de MyMemory).
- Metron (cuenta gratuita) acepta unas 20 peticiones por minuto y abrir un cómic cuesta varias (el cómic y la imagen de cada personaje). Si se abren muchos cómics seguidos, la app muestra "Demasiadas búsquedas seguidas. Espera un minuto e intenta de nuevo."

## Seguridad

Medidas aplicadas siguiendo la guía de clase "Autenticación y seguridad con Jetpack Compose":

- **Sin copias de seguridad:** `allowBackup="false"` y reglas de extracción (`res/xml/data_extraction_rules.xml`). Los datos de la app, incluida la sesión, no se copian a la nube ni pasan a otro teléfono. Al reinstalar la app hay que volver a iniciar sesión.
- **Solo HTTPS:** `res/xml/seguridad_red.xml` bloquea cualquier conexión `http://` sin cifrar.
- **Sin permisos de galería:** la foto de la portada se elige con el selector del sistema, que no necesita permisos de almacenamiento.
- **Sin secretos en los logs:** la app no imprime tokens, claves, correos ni contraseñas.

**Limitación conocida: las claves quedan dentro del APK.** `METRON_TOKEN` y `GEMINI_API_KEY` se leen de `local.properties` al compilar y quedan guardadas dentro del APK. `local.properties` solo evita que lleguen a git: cualquiera que tenga el APK puede extraerlas. En una app publicada se resolvería así (no está implementado en este proyecto):

- Metron: un servidor intermediario que guarde el token y haga las consultas por la app.
- Gemini: Firebase AI Logic con App Check, para llamar al modelo sin poner la clave en la app.

**Sobre `google-services.json`:** identifica el proyecto de Firebase; no es una clave secreta. La protección de los datos está en las reglas de Firestore (ver "Configurar Firebase"). Igual se mantiene fuera de git, porque cada quien usa su propio proyecto.

## Próximas fases

- Soporte para ISBN y códigos de ediciones en español.
- Funciones de colección personal.
