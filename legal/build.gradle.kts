plugins {
  id("reminder.android.library")
}

android {
  namespace = "com.github.naz013.legal"
}

dependencies {
  implementation(project(":domain"))
  implementation(project(":logging-api"))
  implementation(project(":legal-api"))

  implementation(platform(libs.firebase.bom))
  implementation(libs.firebase.config)

  implementation(libs.koin.android)
  implementation(libs.koin.android.ext)

  implementation(libs.kotlinx.coroutines.core)
  implementation(libs.gson)

  testImplementation(libs.junit)
  testImplementation(libs.mockk)
  testImplementation(libs.kotlinx.coroutines.test)
}
