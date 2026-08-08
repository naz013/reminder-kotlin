plugins {
  id("reminder.kotlin.jvm")
}

dependencies {
  implementation(project(":logging-api"))
  implementation(project(":domain"))

  implementation(libs.threetenbp)
  implementation(libs.gson)

  testImplementation(libs.junit)
  testImplementation(libs.mockk)
}
