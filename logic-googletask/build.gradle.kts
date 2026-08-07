plugins {
  id("reminder.kotlin.jvm")
}

dependencies {
  implementation(project(":domain"))
  implementation(project(":logging-api"))

  testImplementation(libs.junit)
  testImplementation(libs.mockk)
}
