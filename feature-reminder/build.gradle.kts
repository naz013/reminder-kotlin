plugins {
  id("reminder.android.library.compose")
}

android {
  namespace = "com.github.naz013.feature.reminder"
}

dependencies {
  implementation(project(":domain"))
  implementation(project(":logging-api"))
  implementation(project(":platform-common"))
  implementation(project(":ui-common"))
  implementation(project(":logic-reminder"))
  implementation(project(":analytics"))
  implementation(project(":appwidgets-api"))
  implementation(project(":date-calculations"))
  implementation(project(":ui-reminder"))

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
