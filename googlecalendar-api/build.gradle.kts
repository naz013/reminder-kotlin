plugins {
  id("reminder.kotlin.jvm")
}

dependencies {
  implementation(project(":logging-api"))
  implementation(project(":domain"))
}
