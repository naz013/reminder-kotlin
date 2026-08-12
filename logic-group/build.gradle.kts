plugins {
  id("reminder.kotlin.jvm")
}

dependencies {
  implementation(project(":domain"))
  implementation(project(":logging-api"))
  implementation(project(":repository-api"))
  implementation(project(":platform-api"))
  implementation(project(":date-calculations"))

  implementation(libs.koin.core)

  testImplementation(libs.junit)
  testImplementation(libs.mockk)
}
