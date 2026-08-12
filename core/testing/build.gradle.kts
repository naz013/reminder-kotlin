plugins {
  id("reminder.android.library")
}

android {
  namespace = "com.github.naz013.testing"
}

dependencies {
  implementation(project(":core:logging-api"))
  implementation(project(":core:feature-common"))

  implementation(libs.junit)
  implementation(libs.androidx.test.core)
  implementation(libs.mockk)
  implementation(libs.androidx.core.testing)
  implementation(libs.androidx.lifecycle.runtime.testing)
  implementation(libs.kotlinx.coroutines.test)
  implementation(libs.robolectric)
  implementation(libs.androidx.lifecycle.livedata.ktx)
}
