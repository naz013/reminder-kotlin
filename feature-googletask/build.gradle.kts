plugins {
  id("reminder.android.library.compose")
  alias(libs.plugins.kotlin.serialization)
}

android {
  namespace = "com.github.naz013.feature.googletask"
}

dependencies {
  implementation(project(":domain"))
  implementation(project(":logging-api"))
  implementation(project(":cloud-api"))
  implementation(project(":repository-api"))
  implementation(project(":work-api"))
  implementation(project(":platform-common"))
  implementation(project(":ui-common"))
  implementation(project(":ui-googletask"))
  implementation(project(":feature-common"))
  implementation(project(":usecase:googletasks"))
  implementation(project(":analytics"))
  implementation(project(":appwidgets-api"))
  implementation(project(":logic-googletask"))

  implementation(libs.threetenbp)
  implementation(libs.gson)

  implementation(libs.koin.android)
  implementation(libs.koin.android.ext)
  implementation(libs.koin.androidx.compose)
  implementation(libs.koin.compose.navigation3)

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
  testImplementation(project(":testing"))
}
