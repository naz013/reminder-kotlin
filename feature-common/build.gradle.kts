plugins {
  id("reminder.android.library")
}

android {
  namespace = "com.github.naz013.feature.common"
}

dependencies {
  implementation(project(":domain"))
  implementation(project(":logging-api"))

  implementation(libs.koin.android)
  implementation(libs.koin.android.ext)

  implementation(libs.androidx.core.ktx)

  implementation(libs.kotlinx.coroutines.core)
  implementation(libs.kotlinx.coroutines.android)

  implementation(libs.material)

  implementation(libs.androidx.lifecycle.extensions)
  implementation(libs.androidx.lifecycle.viewmodel.ktx)
  implementation(libs.androidx.lifecycle.livedata.ktx)
}
