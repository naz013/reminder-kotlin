plugins {
  id("reminder.kotlin.jvm")
}

dependencies {
  implementation(project(":core:domain"))
  implementation(libs.threetenbp)
  implementation(libs.kotlinx.coroutines.core)
}
