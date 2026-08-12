plugins {
  id("reminder.android.library")
}

android {
  namespace = "com.github.naz013.legal"
}

dependencies {
  implementation(project(":core:domain"))
  implementation(project(":core:logging-api"))
  implementation(project(":data:legal-api"))

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
