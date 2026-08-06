plugins {
  id("reminder.android.library")
  alias(libs.plugins.ksp)
}

android {
  namespace = "com.github.naz013.repository"
}

dependencies {
  implementation(project(":domain"))
  implementation(project(":repository-api"))
  implementation(project(":logging-api"))

  implementation(libs.koin.android)
  implementation(libs.koin.android.ext)

  implementation(libs.androidx.localbroadcastmanager)

  implementation(libs.androidx.room.ktx)
  implementation(libs.androidx.room.runtime)
  ksp(libs.androidx.room.compiler)

  implementation(libs.gson)
  implementation(libs.threetenbp)

  testImplementation(libs.junit)
  testImplementation(libs.mockk)
  testImplementation(libs.kotlinx.coroutines.test)
}
