plugins {
  id("reminder.android.library.compose")
}

android {
  namespace = "com.github.naz013.ui.agenda"
}

dependencies {
  implementation(project(":core:domain"))
  implementation(project(":core:logging-api"))
  implementation(project(":core:platform-common"))
  implementation(project(":core:date-calculations"))
  implementation(project(":ui:ui-common"))
  implementation(project(":ui:ui-reminder"))
  implementation(project(":ui:ui-birthday"))

  implementation(libs.koin.android)
  implementation(libs.koin.android.ext)

  implementation(libs.threetenbp)

  implementation(platform(libs.compose.bom))
  implementation(libs.compose.material3)
  implementation(libs.compose.foundation.foundation)
  implementation(libs.compose.ui.ui)
  implementation(libs.compose.ui.tooling.preview)
  debugImplementation(libs.compose.ui.tooling)

  testImplementation(libs.junit)
  testImplementation(libs.mockk)
}
