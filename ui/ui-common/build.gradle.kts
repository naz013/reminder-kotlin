plugins {
  id("reminder.android.library.compose")
}

android {
  namespace = "com.github.naz013.ui.common"
  buildFeatures {
    viewBinding = true
  }
}

dependencies {
  implementation(project(":core:domain"))
  implementation(project(":core:logging-api"))
  implementation(project(":core:navigation-api"))
  implementation(project(":core:platform-api"))
  implementation(project(":core:feature-common"))
  implementation(project(":core:platform-common"))
  implementation(project(":core:date-calculations"))

  implementation(libs.koin.android)
  implementation(libs.androidx.core.ktx)
  implementation(libs.androidx.biometric.compose)
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
  implementation(libs.compose.material3.adaptive)
  implementation(libs.compose.material3.adaptive.layout)

  implementation(libs.androidx.window)

  debugImplementation(libs.compose.ui.test.manifest)
  debugImplementation(libs.compose.ui.tooling)

  testImplementation(libs.junit)
  testImplementation(libs.androidx.test.core)
  testImplementation(libs.mockk)
  testImplementation(libs.mockito.core)
  testImplementation(libs.mockito.kotlin)
}
