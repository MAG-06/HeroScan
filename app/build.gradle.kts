import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.google.services)
    alias(libs.plugins.kotlin.serialization) // genera el código de las rutas @Serializable (rutas tipadas)
}

// Lee local.properties para obtener el token de Metron y la clave y el modelo de Gemini sin escribirlos en el código.
val propiedadesLocales = Properties().apply {
    val archivo = rootProject.file("local.properties")
    if (archivo.exists()) archivo.inputStream().use { load(it) }
}

android {
    namespace = "com.example.heroscan"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.example.heroscan"
        minSdk = 24
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        // Crea la constante BuildConfig.METRON_TOKEN con el valor de METRON_TOKEN de local.properties (vacía si no está).
        buildConfigField("String", "METRON_TOKEN", "\"${propiedadesLocales.getProperty("METRON_TOKEN", "")}\"")
        // Crea BuildConfig.GEMINI_API_KEY con la clave de Gemini de local.properties (vacía si no está).
        buildConfigField("String", "GEMINI_API_KEY", "\"${propiedadesLocales.getProperty("GEMINI_API_KEY", "")}\"")
        // Crea BuildConfig.GEMINI_MODEL con el modelo de Gemini a usar; si no está en local.properties usa "gemini-3.5-flash-lite".
        buildConfigField("String", "GEMINI_MODEL", "\"${propiedadesLocales.getProperty("GEMINI_MODEL", "gemini-3.5-flash-lite")}\"")
    }

    buildTypes {
        release {
            optimization {
                enable = false
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
    implementation(libs.navigation.compose)
    implementation(libs.kotlinx.serialization.json) // convierte las rutas @Serializable en argumentos de navegación
    implementation(libs.material.icons.extended)
    implementation(libs.camera.core) // dependencias de la camara
    implementation(libs.camera.camera2)
    implementation(libs.camera.lifecycle)
    implementation(libs.camera.view) // dependencias de la camara
    implementation(libs.mlkit.barcode.scanning) // dependencias de mlkit
    implementation(libs.androidx.lifecycle.viewmodel.compose) // dependencias de viewModel estructura MVVM
    implementation(libs.androidx.lifecycle.runtime.compose) // collectAsStateWithLifecycle: el acelerómetro de Inicio y el estado de los ViewModels
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0") // dependencias de retrofit para la conexion de la apii
    implementation("com.squareup.okhttp3:okhttp:4.12.0") // OkHttp (para el interceptor de autenticación de Metron)
    implementation("io.coil-kt:coil-compose:2.7.0")
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)
    implementation(libs.kotlinx.coroutines.play.services)
}