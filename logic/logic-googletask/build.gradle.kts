plugins {
  id("reminder.kotlin.jvm")
}

dependencies {
  implementation(project(":core:domain"))
  implementation(project(":core:logging-api"))
  implementation(project(":data:repository-api"))
  implementation(project(":data:cloud-api"))

  implementation(libs.koin.core)

  testImplementation(libs.junit)
  testImplementation(libs.mockk)
}
