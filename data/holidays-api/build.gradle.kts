plugins {
  id("reminder.kotlin.jvm")
}

dependencies {
  implementation(project(":core:domain"))
  implementation(libs.kotlinx.coroutines.core)
  implementation(libs.threetenbp)

  testImplementation(libs.junit)
}
