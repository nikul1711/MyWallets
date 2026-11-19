// Top-level build file. Plugin versions are declared once here; the app module applies them.
plugins {
    id("com.android.application") version "8.13.2" apply false
    id("org.jetbrains.kotlin.android") version "2.3.21" apply false
    id("com.google.devtools.ksp") version "2.3.14" apply false
    id("org.jetbrains.kotlin.plugin.serialization") version "2.3.21" apply false // needed by the Supabase SDK
}
