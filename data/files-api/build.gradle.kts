plugins {
  id("reminder.kotlin.jvm")
}

dependencies {
  implementation(project(":core:domain"))
  implementation(project(":core:logging-api"))

  implementation(libs.gson)

  testImplementation(libs.junit)
}
