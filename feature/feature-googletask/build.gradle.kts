plugins {
  id("reminder.android.library.compose")
  alias(libs.plugins.kotlin.serialization)
}

android {
  namespace = "com.github.naz013.feature.googletask"

  testOptions {
    unitTests {
      isReturnDefaultValues = true
    }
  }
}

dependencies {
  implementation(project(":core:domain"))

  implementation(project(":core:logging-api"))
  implementation(project(":data:cloud-api"))
  implementation(project(":data:repository-api"))
  implementation(project(":data:work-api"))
  implementation(project(":extensions:appwidgets-api"))
  implementation(project(":core:platform-api"))

  implementation(project(":core:platform-common"))
  implementation(project(":ui:ui-common"))
  implementation(project(":ui:ui-googletask"))
  implementation(project(":ui:ui-tag"))
  implementation(project(":core:feature-common"))
  implementation(project(":feature:feature-tags"))
  implementation(project(":core:analytics"))
  implementation(project(":core:date-calculations"))
  implementation(project(":logic:logic-googletask"))
  implementation(project(":logic:logic-reminder"))
  implementation(project(":logic:logic-tag"))

  implementation(libs.play.services.auth)

  implementation(libs.threetenbp)
  implementation(libs.gson)

  implementation(libs.koin.android)
  implementation(libs.koin.android.ext)
  implementation(libs.koin.androidx.compose)
  implementation(libs.koin.compose.navigation3)

  implementation(libs.androidx.navigation3.runtime)
  implementation(libs.androidx.navigation3.ui)
  implementation(libs.androidx.lifecycle.viewmodel.navigation3)
  implementation(libs.kotlinx.serialization.core)

  implementation(platform(libs.compose.bom))
  implementation(libs.compose.material3)
  implementation(libs.compose.material.iconsext)
  implementation(libs.compose.foundation.foundation)
  implementation(libs.compose.ui.ui)
  implementation(libs.compose.ui.tooling.preview)
  debugImplementation(libs.compose.ui.tooling)

  testImplementation(libs.junit)
  testImplementation(libs.mockk)
  testImplementation(libs.kotlinx.coroutines.test)
  testImplementation(project(":core:testing"))
}
