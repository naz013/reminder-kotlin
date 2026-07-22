plugins {
  id("reminder.android.library")
}

android {
  namespace = "com.github.naz013.analytics"
}

dependencies {
  implementation(project(":domain"))
  implementation(project(":logging-api"))
  implementation(libs.androidx.core.ktx)
  implementation(platform(libs.firebase.bom))
  implementation(libs.firebase.analytics)

  testImplementation(libs.junit)
}
