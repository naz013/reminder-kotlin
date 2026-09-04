plugins {
  id("reminder.android.library.compose")
}

android {
  namespace = "com.github.naz013.ui.googletask"
}

dependencies {
  implementation(project(":core:domain"))
  implementation(project(":core:logging-api"))
  implementation(project(":data:cloud-api"))
  implementation(project(":ui:ui-common"))
  implementation(project(":core:platform-common"))

  implementation(libs.google.api.client.android) {
    exclude(group = "org.apache.httpcomponents")
  }

  implementation(libs.play.services.auth)

  implementation(libs.koin.android)
  implementation(libs.koin.androidx.compose)

  implementation(libs.androidx.core.ktx)
  implementation(libs.material)

  implementation(libs.threetenbp)

  implementation(platform(libs.compose.bom))
  implementation(libs.compose.material3)
  implementation(libs.compose.foundation.foundation)
  implementation(libs.compose.ui.tooling.preview)
  implementation(libs.compose.material.iconsext)
  implementation(libs.androidx.activity.compose)

  implementation(libs.compose.material3.windowsizeclass)
  implementation(libs.compose.material3.adaptive.navigation.suite)

  debugImplementation(libs.compose.ui.test.manifest)
  debugImplementation(libs.compose.ui.tooling)

  testImplementation(libs.junit)
  testImplementation(libs.androidx.test.core)
  testImplementation(libs.mockk)
  testImplementation(libs.mockito.core)
  testImplementation(libs.mockito.kotlin)
}
