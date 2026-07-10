plugins {
  id("reminder.kotlin.jvm")
}

dependencies {
  implementation(libs.threetenbp)

  testImplementation(libs.junit)
  testImplementation(libs.mockk)
}
