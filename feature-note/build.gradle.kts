plugins {
  id("reminder.android.library.compose")
  alias(libs.plugins.kotlin.serialization)
}

android {
  namespace = "com.github.naz013.feature.note"
}

dependencies {
  implementation(project(":domain"))
  implementation(project(":logging-api"))
  implementation(project(":platform-common"))
  implementation(project(":platform-api"))
  implementation(project(":ui-common"))
  implementation(project(":ui-note"))
  implementation(project(":repository-api"))
  implementation(project(":feature-common"))
  implementation(project(":files-api"))
  implementation(project(":logic-schedule"))
  implementation(project(":logic-tag"))
  implementation(project(":ui-tag"))
  implementation(project(":logic-reminder"))
  implementation(project(":feature-tags"))
  implementation(project(":analytics"))
  implementation(project(":navigation-api"))
  implementation(project(":appwidgets-api"))
  implementation(project(":date-calculations"))

  implementation(libs.koin.android)
  implementation(libs.koin.android.ext)
  implementation(libs.koin.androidx.compose)
  implementation(libs.koin.compose.navigation3)

  implementation(libs.androidx.navigation3.runtime)
  implementation(libs.androidx.navigation3.ui)
  implementation(libs.androidx.lifecycle.viewmodel.navigation3)
  implementation(libs.kotlinx.serialization.core)

  implementation(libs.coil)
  implementation(libs.coil.compose)

  implementation(libs.threetenbp)

  implementation(libs.lottie)
  implementation(libs.lottie.compose)
  implementation(libs.telephoto.zoomable.image.coil)
  implementation(libs.pdfbox.android)

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
  testImplementation(project(":testing"))
}
