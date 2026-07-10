plugins {
  id("reminder.android.library")
}

android {
  namespace = "com.github.naz013.logging"
}

dependencies {
  implementation(project(":logging-api"))
  implementation(libs.slf4j.api)
  implementation(libs.logback.android)

  implementation(platform(libs.firebase.bom))
  implementation(libs.firebase.crashlytics)

  testImplementation(libs.logback.classic)
}
