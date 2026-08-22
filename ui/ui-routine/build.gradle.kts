plugins {
  id("reminder.android.library.compose")
}

android {
  namespace = "com.github.naz013.ui.routine"
}

dependencies {
  implementation(project(":core:domain"))
  implementation(project(":core:logging-api"))
  implementation(project(":ui:ui-common"))

  implementation(libs.koin.android)
  implementation(libs.koin.androidx.compose)

  implementation(platform(libs.compose.bom))
  implementation(libs.compose.material3)
  implementation(libs.compose.foundation.foundation)
  implementation(libs.compose.ui.ui)
  implementation(libs.compose.ui.tooling.preview)
  debugImplementation(libs.compose.ui.tooling)

  testImplementation(libs.junit)
  testImplementation(libs.mockk)
}
