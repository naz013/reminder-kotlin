plugins {
  id("reminder.android.library")
}

android {
  namespace = "com.github.naz013.ui.birthday"
}

dependencies {
  implementation(project(":core:domain"))
  implementation(project(":data:repository-api"))
  implementation(project(":core:platform-api"))
  implementation(project(":core:platform-common"))
  implementation(project(":ui:ui-common"))
  implementation(project(":core:date-calculations"))

  implementation(libs.koin.core)
  implementation(libs.androidx.core.ktx)
  implementation(libs.threetenbp)

  testImplementation(libs.junit)
  testImplementation(libs.mockk)
}
