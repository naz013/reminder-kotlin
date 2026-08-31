plugins {
  id("reminder.android.library.compose")
  alias(libs.plugins.kotlin.serialization)
}

android {
  namespace = "com.github.naz013.feature.places"

  testOptions {
    unitTests {
      isReturnDefaultValues = true
    }
  }
}

dependencies {
  implementation(project(":core:domain"))
  implementation(project(":data:repository-api"))
  implementation(project(":core:logging-api"))
  implementation(project(":core:navigation-api"))
  implementation(project(":core:platform-common"))
  implementation(project(":core:date-calculations"))
  implementation(project(":core:feature-common"))
  implementation(project(":data:files-api"))
  implementation(project(":data:files"))
  implementation(project(":logic:logic-schedule"))
  implementation(project(":ui:ui-common"))
  implementation(project(":ui:ui-map"))

  implementation(libs.koin.android)
  implementation(libs.koin.android.ext)
  implementation(libs.koin.androidx.compose)
  implementation(libs.koin.compose.navigation3)

  implementation(libs.androidx.navigation3.runtime)
  implementation(libs.androidx.navigation3.ui)
  implementation(libs.androidx.lifecycle.viewmodel.navigation3)
  implementation(libs.compose.material3.adaptive.navigation3)
  implementation(libs.kotlinx.serialization.core)
  implementation(libs.threetenbp)

  implementation(platform(libs.compose.bom))
  implementation(libs.compose.material3)
  implementation(libs.compose.material.iconsext)
  implementation(libs.compose.foundation.foundation)
  implementation(libs.compose.ui.ui)
  implementation(libs.compose.ui.tooling.preview)
  implementation(libs.compose.runtime.livedata)
  debugImplementation(libs.compose.ui.tooling)

  testImplementation(project(":core:testing"))
  testImplementation(libs.junit)
  testImplementation(libs.mockk)
  testImplementation(libs.kotlinx.coroutines.test)
}
