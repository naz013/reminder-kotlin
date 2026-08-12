plugins {
  id("reminder.android.library")
}

android {
  namespace = "com.github.naz013.ui.notification.settings"
}

dependencies {
  implementation(project(":domain"))
  implementation(project(":repository-api"))
  implementation(project(":platform-common"))
  implementation(project(":ui-common"))

  testImplementation(libs.junit)
  testImplementation(libs.mockk)
}
