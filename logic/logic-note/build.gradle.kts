plugins {
  id("reminder.kotlin.jvm")
}

dependencies {
  implementation(project(":core:domain"))
  implementation(project(":core:logging-api"))

  testImplementation(libs.junit)
  testImplementation(libs.mockk)
}
