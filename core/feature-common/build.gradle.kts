plugins {
  id("reminder.android.library")
}

android {
  namespace = "com.github.naz013.feature.common"
}

// Detekt 1.23.x bundles a pre-context-parameters Kotlin compiler frontend and crashes
// parsing `context(...)` syntax (https://github.com/detekt/detekt/issues/8691). Excluded
// until we move to detekt 2.0 (which targets Kotlin 2.4+).
tasks.matching { it.name.startsWith("detekt") }.configureEach {
  if (this is SourceTask) {
    exclude("**/ViewModelExtensions.kt")
  }
}

dependencies {
  implementation(project(":core:domain"))
  implementation(project(":core:logging-api"))

  implementation(libs.koin.android)
  implementation(libs.koin.android.ext)

  implementation(libs.androidx.core.ktx)

  implementation(libs.kotlinx.coroutines.core)
  implementation(libs.kotlinx.coroutines.android)

  implementation(libs.material)

  implementation(libs.androidx.lifecycle.extensions)
  implementation(libs.androidx.lifecycle.viewmodel.ktx)
  implementation(libs.androidx.lifecycle.livedata.ktx)

  testImplementation(libs.junit)
  testImplementation(libs.mockk)
}
