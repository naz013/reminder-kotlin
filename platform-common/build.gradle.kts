plugins {
  id("reminder.android.library.compose")
}

android {
  namespace = "com.github.naz013.common"
}

dependencies {
  implementation(project(":logging-api"))
  implementation(project(":platform-api"))
  implementation(project(":feature-common"))
  implementation(project(":date-calculations"))

  implementation(libs.koin.android)
  implementation(libs.koin.android.ext)

  implementation(libs.play.services.auth)

  implementation(libs.threetenbp)

  implementation(libs.androidx.biometric)

  implementation(platform(libs.compose.bom))
  implementation(libs.compose.ui.ui)
  implementation(libs.androidx.activity.compose)

  testImplementation(libs.junit)
  testImplementation(libs.mockk)
}
