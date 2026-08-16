plugins {
  id("reminder.android.library.compose")
}

android {
  namespace = "com.github.naz013.ui.map"

  testOptions {
    unitTests {
      isReturnDefaultValues = true
    }
  }
}

dependencies {
  implementation(project(":core:domain"))
  implementation(project(":core:logging-api"))
  implementation(project(":core:feature-common"))
  implementation(project(":core:feature-flags-api"))
  implementation(project(":core:platform-common"))
  implementation(project(":core:platform-api"))
  implementation(project(":core:date-calculations"))
  implementation(project(":data:repository-api"))
  implementation(project(":ui:ui-common"))

  implementation(libs.koin.android)
  implementation(libs.koin.android.ext)
  implementation(libs.koin.androidx.compose)

  implementation(libs.threetenbp)

  // LatLng et al. are part of MapMarker/MarkerState/GeocoderTask's public surface, so consumers
  // (feature-reminder, places) need it transitively rather than redeclaring it themselves.
  api(libs.play.services.maps)
  implementation(libs.maps.compose)

  implementation(platform(libs.compose.bom))
  implementation(libs.compose.material3)
  implementation(libs.compose.foundation.foundation)
  implementation(libs.compose.ui.ui)
  implementation(libs.compose.ui.tooling.preview)
  debugImplementation(libs.compose.ui.tooling)

  testImplementation(project(":core:testing"))
  testImplementation(libs.junit)
  testImplementation(libs.mockk)
  testImplementation(libs.kotlinx.coroutines.test)
}
