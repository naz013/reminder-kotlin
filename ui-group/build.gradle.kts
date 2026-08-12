plugins {
  id("reminder.android.library")
}

android {
  namespace = "com.github.naz013.ui.group"
}

dependencies {
  implementation(project(":domain"))
  implementation(project(":repository-api"))
  implementation(project(":platform-api"))
  implementation(project(":platform-common"))
  implementation(project(":ui-common"))
  implementation(project(":date-calculations"))

  implementation(libs.androidx.core.ktx)
  implementation(libs.threetenbp)

  testImplementation(libs.junit)
  testImplementation(libs.mockk)
}
