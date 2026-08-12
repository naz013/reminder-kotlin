plugins {
  id("reminder.android.library")
}

android {
  namespace = "com.github.naz013.files"
}

dependencies {
  implementation(project(":core:domain"))
  implementation(project(":core:logging-api"))
  implementation(project(":data:files-api"))

  implementation(libs.koin.android)

  implementation(libs.gson)
  implementation(libs.threetenbp)

  testImplementation(libs.junit)
}
