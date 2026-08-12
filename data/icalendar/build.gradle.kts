plugins {
  id("reminder.android.library")
}

android {
  namespace = "com.github.naz013.icalendar"
}

dependencies {
  implementation(project(":core:domain"))
  implementation(project(":core:logging-api"))
  implementation(project(":data:icalendar-api"))

  implementation(libs.koin.android)

  implementation(libs.gson)
  implementation(libs.threetenbp)
  implementation(libs.lib.recur)

  testImplementation(libs.junit)
  testImplementation(libs.mockk)
  testImplementation(libs.mockito.core)
  testImplementation(libs.mockito.kotlin)
}
