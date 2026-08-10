plugins {
  id("reminder.kotlin.jvm")
}

dependencies {
  implementation(project(":domain"))
  implementation(libs.kotlinx.coroutines.core)
  implementation(libs.threetenbp)

  testImplementation(libs.junit)
}
