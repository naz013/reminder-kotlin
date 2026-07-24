plugins {
  id("reminder.kotlin.jvm")
}

dependencies {
  implementation(libs.threetenbp)

  implementation(libs.koin.core)

  testImplementation(libs.junit)
  testImplementation(libs.mockk)
}
