import com.android.build.api.dsl.ApplicationExtension

plugins {
  id("reminder.android.application.compose")
}

extensions.configure<ApplicationExtension> {
  namespace = "com.github.naz013.wear"
  flavorDimensions.add("level")

  defaultConfig {
    // Wear OS 3+ only - the standalone-app pairing model this companion relies on isn't
    // supported on the older embedded/classic Wear OS releases below API 30.
    minSdk = 30
    versionCode = 1
    versionName = "1.0.0"
  }

  // A Wear OS app is published under the *same* Play Console listing as its phone companion via
  // Test and release > Advanced Settings > Form factors > Add Wear OS - which requires the two
  // APKs to share an applicationId, per flavor. (The older approach of embedding a wear APK
  // inside the phone APK via a `wearApp` Gradle dependency was removed in AGP 9.0 - Play no
  // longer supports it - so matching applicationId here is the whole of what's left to wire up
  // in code; the actual publishing link is a Play Console step, not a Gradle one.)
  productFlavors {
    create("free") {
      dimension = "level"
      applicationId = "com.cray.software.justreminder"
    }
    create("pro") {
      dimension = "level"
      applicationId = "com.cray.software.justreminderpro"
    }
  }
}

dependencies {
  implementation(project(":extensions:wearsync-api"))
  implementation(project(":core:logging-api"))

  implementation(libs.play.services.wearable)
  implementation(libs.kotlinx.coroutines.play.services)

  implementation(libs.androidx.wear.compose.material3)
  implementation(libs.androidx.wear.compose.foundation)
  implementation(libs.androidx.wear.tiles)
  implementation(libs.androidx.wear.protolayout.material3)
  implementation(libs.androidx.activity.compose)
  implementation(libs.androidx.core.ktx)
  implementation(libs.androidx.lifecycle.viewmodel.ktx)

  implementation(platform(libs.compose.bom))
  implementation(libs.compose.ui.ui)
  implementation(libs.compose.ui.tooling.preview)
  debugImplementation(libs.compose.ui.tooling)
}
