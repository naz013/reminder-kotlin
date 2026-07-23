plugins {
  id("reminder.kotlin.jvm")
}

dependencies {
  implementation(project(":logging-api"))
  implementation(project(":domain"))
  implementation(project(":repository-api"))

  implementation(libs.koin.core)
  implementation(libs.threetenbp)

  testImplementation(libs.junit)
  testImplementation(libs.kotlinx.coroutines.test)
}
