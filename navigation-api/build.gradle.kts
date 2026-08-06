plugins {
  id("reminder.android.library")
  alias(libs.plugins.kotlin.parcelize)
}

android {
  namespace = "com.github.naz013.navigation"
}

dependencies {
  implementation(project(":logging-api"))
  implementation(libs.koin.android)

  testImplementation(libs.junit)
  testImplementation(libs.androidx.test.core)
}
