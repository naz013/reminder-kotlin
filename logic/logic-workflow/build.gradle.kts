plugins {
  id("reminder.kotlin.jvm")
}

dependencies {
  implementation(project(":core:logging-api"))
  implementation(project(":core:domain"))
  implementation(project(":data:repository-api"))
  implementation(project(":data:work-api"))

  implementation(libs.koin.core)
  implementation(libs.threetenbp)

  testImplementation(libs.junit)
  testImplementation(libs.mockk)
  testImplementation(libs.kotlinx.coroutines.test)
}
