plugins {
  id("reminder.android.library")
}

android {
  namespace = "com.github.naz013.files"
}

dependencies {
  implementation(project(":domain"))
  implementation(project(":logging-api"))
  implementation(project(":files-api"))

  implementation(libs.koin.android)

  implementation(libs.gson)
  implementation(libs.threetenbp)

  testImplementation(libs.junit)
}
