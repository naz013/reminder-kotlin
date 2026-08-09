plugins {
  id("reminder.kotlin.jvm")
}

dependencies {
  implementation(project(":domain"))
  implementation(project(":logging-api"))
  implementation(project(":repository-api"))
  implementation(project(":files-api"))
  implementation(project(":logic-schedule"))

  implementation(libs.koin.core)

  testImplementation(libs.junit)
  testImplementation(libs.mockk)
}
