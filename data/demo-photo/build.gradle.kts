plugins {
  id("reminder.android.library")
}

android {
  namespace = "com.github.naz013.demophoto.impl"
}

dependencies {
  implementation(project(":data:demo-photo-api"))
  implementation(project(":core:logging-api"))

  implementation(libs.koin.android)
  implementation(libs.retrofit)
  implementation(libs.retrofit.converter.gson)
  implementation(libs.gson)

  testImplementation(libs.junit)
  testImplementation(libs.mockk)
  testImplementation(libs.kotlinx.coroutines.test)
}
