plugins {
  id("reminder.kotlin.jvm")
}

dependencies {
  implementation(project(":core:logging-api"))

  implementation(libs.threetenbp)

  implementation(libs.koin.core)

  testImplementation(libs.junit)
  testImplementation(libs.mockk)
}
