plugins {
  id("reminder.android.library.compose")
}

android {
  namespace = "com.github.naz013.feature.note"
}

dependencies {
  implementation(project(":domain"))
  implementation(project(":logging-api"))
  implementation(project(":platform-common"))
  implementation(project(":ui-common"))

  implementation(libs.koin.android)
  implementation(libs.koin.android.ext)

  implementation(libs.coil)
  implementation(libs.coil.compose)

  implementation(platform(libs.compose.bom))
  implementation(libs.compose.material3)
  implementation(libs.compose.foundation.foundation)
  implementation(libs.compose.ui.tooling.preview)

  debugImplementation(libs.compose.ui.tooling)

  testImplementation(libs.junit)
  testImplementation(libs.mockk)
}
