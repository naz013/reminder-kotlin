import com.android.build.api.dsl.ApplicationExtension

plugins {
  id("reminder.android.application.compose")
}

extensions.configure<ApplicationExtension> {
  namespace = "com.github.naz013.wear"

  defaultConfig {
    applicationId = "com.cray.software.justreminder.wear"
    // Wear OS 3+ only - the standalone-app pairing model this companion relies on isn't
    // supported on the older embedded/classic Wear OS releases below API 30.
    minSdk = 30
    versionCode = 1
    versionName = "1.0.0"
  }
}

dependencies {
  implementation(project(":extensions:wearsync-api"))
  implementation(project(":core:logging-api"))

  implementation(libs.play.services.wearable)
  implementation(libs.kotlinx.coroutines.play.services)

  implementation(libs.androidx.wear.compose.material)
  implementation(libs.androidx.wear.compose.foundation)
  implementation(libs.androidx.activity.compose)
  implementation(libs.androidx.core.ktx)
  implementation(libs.androidx.lifecycle.viewmodel.ktx)

  implementation(platform(libs.compose.bom))
  implementation(libs.compose.ui.ui)
  implementation(libs.compose.ui.tooling.preview)
  debugImplementation(libs.compose.ui.tooling)
}
