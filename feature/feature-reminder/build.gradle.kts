plugins {
  id("reminder.android.library.compose")
}

android {
  namespace = "com.github.naz013.feature.reminder"
}

dependencies {
  implementation(project(":core:domain"))
  implementation(project(":core:logging-api"))
  implementation(project(":core:platform-common"))
  implementation(project(":ui:ui-common"))
  implementation(project(":logic:logic-reminder"))
  implementation(project(":core:analytics"))
  implementation(project(":extensions:appwidgets-api"))
  implementation(project(":core:date-calculations"))
  implementation(project(":ui:ui-reminder"))

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
