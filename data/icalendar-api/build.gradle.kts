plugins {
  id("reminder.kotlin.jvm")
}

dependencies {
  implementation(project(":core:logging-api"))
  implementation(project(":core:domain"))

  implementation(libs.threetenbp)
  implementation(libs.gson)

  testImplementation(libs.junit)
  testImplementation(libs.mockk)
}
