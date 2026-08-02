plugins {
  id("reminder.android.library.compose")
  alias(libs.plugins.kotlin.serialization)
}

android {
  namespace = "com.github.naz013.insights"
}

dependencies {
  implementation(project(":domain"))
  implementation(project(":repository-api"))
  implementation(project(":logging-api"))
  implementation(project(":feature-common"))
  implementation(project(":platform-common"))
  implementation(project(":ui-common"))

  implementation(libs.koin.android)
  implementation(libs.koin.android.ext)
  implementation(libs.koin.androidx.compose)
  implementation(libs.koin.compose.navigation3)

  implementation(libs.threetenbp)

  implementation(libs.androidx.navigation3.runtime)
  implementation(libs.androidx.navigation3.ui)
  implementation(libs.androidx.lifecycle.viewmodel.navigation3)
  implementation(libs.kotlinx.serialization.core)

  implementation(platform(libs.compose.bom))
  implementation(libs.compose.material3)
  implementation(libs.compose.material.iconsext)
  implementation(libs.compose.foundation.foundation)
  implementation(libs.compose.ui.ui)
  implementation(libs.compose.ui.tooling.preview)
  debugImplementation(libs.compose.ui.tooling)

  testImplementation(libs.junit)
  testImplementation(libs.mockk)
  testImplementation(libs.kotlinx.coroutines.test)
}
