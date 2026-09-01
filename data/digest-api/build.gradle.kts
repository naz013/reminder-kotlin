plugins {
  id("reminder.kotlin.jvm")
}

dependencies {
  implementation(project(":core:domain"))
  implementation(libs.kotlinx.coroutines.core)

  testImplementation(libs.junit)
}
