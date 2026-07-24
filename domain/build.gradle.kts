plugins {
  id("reminder.kotlin.jvm")
}

dependencies {
  implementation(libs.gson)
  implementation(libs.threetenbp)

  testImplementation(libs.junit)
}
