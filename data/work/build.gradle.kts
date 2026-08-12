plugins {
  id("reminder.android.library")
}

android {
  namespace = "com.github.naz013.work"
}

dependencies {
  implementation(project(":data:work-api"))
  implementation(project(":core:logging-api"))
  implementation(project(":core:feature-common"))

  implementation(libs.androidx.work.runtime) {
    exclude(group = "com.google.guava", module = "listenablefuture")
  }
  implementation(libs.androidx.work.runtime.ktx) {
    exclude(group = "com.google.guava", module = "listenablefuture")
  }

  implementation(libs.koin.android)
  implementation(libs.koin.androidx.workmanager)

  implementation(libs.kotlinx.coroutines.core)

  testImplementation(libs.junit)
  testImplementation(libs.mockk)
  testImplementation(libs.kotlinx.coroutines.test)
}
