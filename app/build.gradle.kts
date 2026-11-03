import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
}

android {
    namespace = "br.edu.ifrs.genki"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "br.edu.ifrs.genki"
        minSdk = 26
        targetSdk = 37
        versionCode = 25
        versionName = "1.0.24"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // APK menor para um aparelho só: `-Pgenki.abi=arm64-v8a` (Samsung A31; o ADB pelo cabo cai em envios grandes).
        // Sem a propriedade, o APK leva as bibliotecas do ML Kit para todos os processadores (emulador incluído).
        providers.gradleProperty("genki.abi").orNull?.let { abi -> ndk { abiFilters += abi } }
    }

    // Chave do APK de distribuição (V1 para teste informal): fica fora do Git e é lida de `local.properties`
    // (genki.release.storeFile/storePassword/keyAlias/keyPassword). Sem ela, o release sai sem assinatura.
    val localProps = Properties().apply {
        rootProject.file("local.properties").takeIf { it.exists() }?.inputStream()?.use { load(it) }
    }
    val releaseStoreFile = localProps.getProperty("genki.release.storeFile")
    if (releaseStoreFile != null) {
        signingConfigs.create("release") {
            storeFile = file(releaseStoreFile)
            storePassword = localProps.getProperty("genki.release.storePassword")
            keyAlias = localProps.getProperty("genki.release.keyAlias")
            keyPassword = localProps.getProperty("genki.release.keyPassword")
        }
    }

    buildTypes {
        release {
            signingConfigs.findByName("release")?.let { signingConfig = it }
            // Só processadores de celular: o OCR do ML Kit para x86/x86_64 (emulador de PC) somava ~23 MB ao APK
            // de distribuição (pedido do autor, 30/10/2026). O debug continua com todos, para o emulador.
            ndk { abiFilters += listOf("arm64-v8a", "armeabi-v7a") }
            // Otimização (R8) desligada: a parcial do template (`packageScope` androidx/kotlin) gerava
            // IllegalAccessError ao abrir o app (A31, 28/10/2026). Sem ela, o release roda o mesmo código testado no debug.
            optimization {
                enable = false
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    // Testes do ViewModel da voz: `Log.d` do GenkiEval devolve o padrão em vez de falhar fora do Android.
    testOptions {
        unitTests.isReturnDefaultValues = true
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

// Esquema do banco exportado por versão (`app/schemas`), base para escrever e conferir migrações.
ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.core)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)
    implementation(libs.androidx.camera.camera2)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.camera.view)
    implementation(libs.mlkit.text.recognition)
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}