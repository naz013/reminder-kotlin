plugins {
  id("reminder.android.library")
}

android {
  namespace = "com.github.naz013.ui.notification.settings"
}

dependencies {
  implementation(project(":core:domain"))
  implementation(project(":data:repository-api"))
  implementation(project(":core:platform-common"))
  implementation(project(":ui:ui-common"))

  testImplementation(libs.junit)
  testImplementation(libs.mockk)
}
