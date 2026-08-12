plugins {
  id("reminder.kotlin.jvm")
}

dependencies {
  implementation(project(":core:logging-api"))
  implementation(project(":core:domain"))

  testImplementation(libs.junit)
}
