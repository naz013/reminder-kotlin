plugins {
  id("reminder.android.library")
}

android {
  namespace = "com.github.naz013.icalendar"
}

dependencies {
  implementation(project(":domain"))
  implementation(project(":logging-api"))
  implementation(project(":icalendar-api"))

  implementation(libs.koin.android)

  implementation(libs.gson)
  implementation(libs.threetenbp)
  implementation(libs.lib.recur)

  testImplementation(libs.junit)
  testImplementation(libs.mockk)
  testImplementation(libs.mockito.core)
  testImplementation(libs.mockito.kotlin)
}
