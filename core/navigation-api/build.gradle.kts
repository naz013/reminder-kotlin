plugins {
  id("reminder.android.library")
  alias(libs.plugins.kotlin.parcelize)
}

android {
  namespace = "com.github.naz013.navigation"
}

dependencies {
  implementation(project(":core:logging-api"))
  implementation(libs.koin.android)
  implementation(libs.androidx.navigation3.runtime)

  testImplementation(libs.junit)
  testImplementation(libs.androidx.test.core)
}
