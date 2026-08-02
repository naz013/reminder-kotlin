plugins {
  id("reminder.android.library.compose")
  alias(libs.plugins.ksp)
}

android {
  namespace = "com.github.naz013.tags"
}

dependencies {
  implementation(project(":tags-api"))
  implementation(project(":domain"))
  implementation(project(":logging-api"))
  implementation(project(":ui-common"))

  implementation(libs.koin.android)
  implementation(libs.koin.android.ext)
  implementation(libs.koin.androidx.compose)

  implementation(libs.androidx.room.ktx)
  implementation(libs.androidx.room.runtime)
  ksp(libs.androidx.room.compiler)

  implementation(platform(libs.compose.bom))
  implementation(libs.compose.material3)
  implementation(libs.compose.foundation.foundation)
  implementation(libs.compose.ui.ui)
  implementation(libs.compose.ui.tooling.preview)
  debugImplementation(libs.compose.ui.tooling)

  testImplementation(libs.junit)
  testImplementation(libs.mockk)
  testImplementation(libs.kotlinx.coroutines.test)
  testImplementation(libs.robolectric)
  testImplementation(libs.androidx.test.core)
}
