# HeroScan — Proyecto Universitario Android

## Descripción
Aplicación Android para escanear códigos de barras de cómics físicos y obtener información relevante. Proyecto universitario individual, desarrollo progresivo durante el semestre.

## Tecnología
- Android Studio Quail 3 (2026.1.3)
- Kotlin
- Jetpack Compose (UI)
- Material Design 3
- CameraX (cámara funcionando, vista previa en vivo integrada en ScanScreen)
- Google ML Kit Barcode Scanning (integrado, detecta códigos de barras en tiempo real desde la cámara)
- Retrofit (consumo de API, pendiente de implementar)
- Android Sensor Framework (sensores, pendiente de implementar)
- Navigation Compose (navegación entre pantallas)

## Arquitectura
MVVM sencilla según lo aprendido en clase:
```
UI / Compose → ViewModel → Repository → Retrofit / API
```
Por ahora solo existe la capa UI. ViewModel, Repository y Retrofit se agregan cuando se conecte la API real.

## Estructura actual del proyecto
```
com.example.heroscan
├── MainActivity.kt          (NavHost con rutas: home, comicDetail/{comicId}, scan)
├── ui.theme/                 (paleta oscura fija: fondo, superficie, cian y magenta; sin modo claro ni dynamic color)
├── ui.home/
│   └── HomeScreen.kt        (pantalla principal, diseño de Figma implementado, pide permiso de cámara antes de escanear)
├── ui.scan/
│   └── ScanScreen.kt        (pantalla de escaneo: cámara en vivo con CameraX + detección de código de barras con ML Kit)
├── ui.detail/
│   └── ComicDetailScreen.kt (pantalla de detalle, placeholder con datos mock)
└── model/
    ├── Comic.kt              (data class del cómic)
    └── MockComics.kt         (datos de ejemplo temporales)

res/drawable/
└── imageback_scan_card.jpg  (imagen de fondo usada en HomeScreen y ScanScreen)
```

## Rutas de navegación (NavHost)
- `"home"` → HomeScreen (pantalla principal)
- `"comicDetail/{comicId}"` → ComicDetailScreen (detalle del cómic)
- `"scan"` → ScanScreen (pantalla de escaneo con cámara)

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
6. Siguiente paso: investigar y conectar API de cómics

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