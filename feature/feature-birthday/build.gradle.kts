plugins {
  id("reminder.android.library.compose")
  alias(libs.plugins.kotlin.serialization)
}

android {
  namespace = "com.github.naz013.feature.birthday"
}

dependencies {
  implementation(project(":core:domain"))
  implementation(project(":data:repository-api"))
  implementation(project(":core:logging-api"))
  implementation(project(":core:navigation-api"))
  implementation(project(":core:analytics"))
  implementation(project(":core:platform-common"))
  implementation(project(":core:platform-api"))
  implementation(project(":core:date-calculations"))
  implementation(project(":core:feature-common"))
  implementation(project(":data:work-api"))
  implementation(project(":data:scheduler-api"))
  implementation(project(":extensions:appwidgets-api"))
  implementation(project(":ui:ui-common"))
  implementation(project(":ui:ui-birthday"))
  implementation(project(":ui:ui-agenda"))
  implementation(project(":ui:ui-tag"))
  implementation(project(":logic:logic-birthday"))
  implementation(project(":logic:logic-reminder"))
  implementation(project(":logic:logic-tag"))
  implementation(project(":feature:feature-tags"))

  implementation(libs.koin.android)
  implementation(libs.koin.android.ext)
  implementation(libs.koin.androidx.compose)
  implementation(libs.koin.compose.navigation3)

  implementation(libs.androidx.navigation3.runtime)
  implementation(libs.androidx.navigation3.ui)
  implementation(libs.androidx.lifecycle.viewmodel.navigation3)
  implementation(libs.kotlinx.serialization.core)
  implementation(libs.threetenbp)
  implementation(libs.compose.material3.adaptive.navigation3)

  implementation(platform(libs.compose.bom))
  implementation(libs.compose.material3)
  implementation(libs.compose.material.iconsext)
  implementation(libs.compose.foundation.foundation)
  implementation(libs.compose.ui.ui)
  implementation(libs.compose.ui.tooling.preview)
  implementation(libs.compose.runtime.livedata)
  debugImplementation(libs.compose.ui.tooling)

  implementation(libs.lottie)
  implementation(libs.lottie.compose)

  testImplementation(project(":core:testing"))
  testImplementation(libs.junit)
  testImplementation(libs.mockk)
  testImplementation(libs.kotlinx.coroutines.test)
}
