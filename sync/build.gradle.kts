plugins {
  id("reminder.kotlin.jvm")
}

dependencies {
  implementation(project(":domain"))
  implementation(project(":cloud-api"))
  implementation(project(":repository-api"))
  implementation(project(":logging-api"))
  implementation(project(":files-api"))

  implementation(libs.koin.core)
  implementation(libs.threetenbp)
  implementation(libs.kotlinx.coroutines.core)

  testImplementation(libs.junit)
  testImplementation(libs.mockk)
  testImplementation(libs.kotlinx.coroutines.test)
}
