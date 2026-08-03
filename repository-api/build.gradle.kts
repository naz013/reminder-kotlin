plugins {
  id("reminder.kotlin.jvm")
}

dependencies {
  implementation(project(":domain"))
  implementation(libs.threetenbp)
  implementation(libs.kotlinx.coroutines.core)
}
