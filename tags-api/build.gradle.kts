plugins {
  id("reminder.kotlin.jvm")
}

dependencies {
  implementation(project(":logging-api"))
  implementation(project(":domain"))

  implementation(libs.koin.core)
  implementation(libs.kotlinx.coroutines.core)

  testImplementation(libs.junit)
  testImplementation(libs.mockk)
  testImplementation(libs.kotlinx.coroutines.test)
}
