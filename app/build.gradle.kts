import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.devtools.ksp") // generates the Room database code at build time
}

android {
    namespace = "com.wallet.mywallet"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.wallet.mywallet"
        minSdk = 26 // Android 8.0 Oreo
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    buildFeatures {
        viewBinding = true // gives every layout a typed "Binding" class, so no findViewById
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
        // Apply annotations such as @StringRes to the property as well as the constructor parameter.
        freeCompilerArgs.add("-Xannotation-default-target=param-property")
    }
}

// Room writes the database schema to JSON so future migrations can be checked.
ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}

dependencies {
    // UI
    implementation("androidx.core:core-ktx:1.17.0")
    implementation("androidx.appcompat:appcompat:1.7.1")
    implementation("com.google.android.material:material:1.13.0")
    implementation("androidx.recyclerview:recyclerview:1.4.0")
    implementation("androidx.fragment:fragment-ktx:1.8.9")

    // MVVM: ViewModel, SavedStateHandle, lifecycle-aware collection
    implementation("androidx.lifecycle:lifecycle-viewmodel:2.10.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-savedstate:2.10.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.10.0")

    // Navigation between screens
    implementation("androidx.navigation:navigation-fragment:2.9.8")
    implementation("androidx.navigation:navigation-ui:2.9.8")

    // Local database
    val roomVersion = "2.8.5"
    implementation("androidx.room:room-runtime:$roomVersion")
    implementation("androidx.room:room-ktx:$roomVersion")
    ksp("androidx.room:room-compiler:$roomVersion")

    // Threading
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.10.2")

    // Daily bill reminders
    implementation("androidx.work:work-runtime-ktx:2.11.0")

    testImplementation("junit:junit:4.13.2")
}
