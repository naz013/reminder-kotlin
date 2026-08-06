plugins {
  id("reminder.kotlin.jvm")
}

dependencies {
  implementation(project(":domain"))
  implementation(project(":logging-api"))

  implementation(libs.gson)

  testImplementation(libs.junit)
}
