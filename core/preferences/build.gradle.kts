plugins {
  id("reminder.android.library")
}

android {
  namespace = "com.github.naz013.preferences"
}

dependencies {
  implementation(libs.gson)

  testImplementation(libs.junit)
}
