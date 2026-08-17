plugins {
  id("reminder.android.library.compose")
  alias(libs.plugins.kotlin.serialization)
}

android {
  namespace = "com.github.naz013.feature.settings"

  testOptions {
    unitTests {
      isReturnDefaultValues = true
    }
  }
}

dependencies {
  implementation(project(":core:domain"))
  implementation(project(":core:analytics"))
  implementation(project(":core:feature-common"))
  implementation(project(":core:feature-flags-api"))
  implementation(project(":core:logging-api"))
  implementation(project(":core:platform-common"))
  implementation(project(":core:platform-api"))
  implementation(project(":core:date-calculations"))
  implementation(project(":data:legal-api"))
  implementation(project(":data:repository-api"))
  implementation(project(":data:work-api"))
  implementation(project(":data:googlecalendar-api"))
  implementation(project(":data:holidays-api"))
  implementation(project(":data:cloud-api"))
  implementation(project(":data:sync"))
  implementation(project(":data:scheduler-api"))
  implementation(project(":data:files-api"))
  implementation(project(":extensions:appwidgets-api"))
  implementation(project(":logic:logic-reminder"))
  implementation(project(":logic:logic-schedule"))
  implementation(project(":logic:logic-googletask"))
  implementation(project(":admin:reviews"))
  implementation(project(":ui:ui-common"))
  implementation(project(":ui:ui-map"))
  implementation(project(":ui:ui-googletask"))

  implementation(libs.material)
  implementation(libs.androidx.activity.compose)
  implementation(libs.lottie)
  implementation(libs.lottie.compose)
  implementation(libs.commons.lang3)
  implementation(libs.lib.recur)
  implementation(libs.threetenbp)
  implementation(libs.play.services.auth)

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

  testImplementation(project(":core:testing"))
  testImplementation(libs.junit)
  testImplementation(libs.mockk)
  testImplementation(libs.kotlinx.coroutines.test)
}
