plugins {
  id("reminder.android.library.compose")
}

android {
  namespace = "com.github.naz013.appwidgets"
}

dependencies {
  implementation(project(":core:domain"))
  implementation(project(":core:logging-api"))
  implementation(project(":core:navigation-api"))
  implementation(project(":data:cloud-api"))
  implementation(project(":data:repository-api"))
  implementation(project(":extensions:appwidgets-api"))
  implementation(project(":core:platform-common"))
  implementation(project(":core:feature-common"))
  implementation(project(":ui:ui-note"))
  implementation(project(":ui:ui-common"))
  implementation(project(":core:analytics"))
  implementation(project(":data:icalendar"))
  implementation(project(":core:date-calculations"))

  implementation(libs.koin.android)
  implementation(libs.koin.android.ext)

  implementation(libs.androidx.core.ktx)

  implementation(libs.kotlinx.coroutines.core)
  implementation(libs.kotlinx.coroutines.android)

  implementation(libs.coil)

  implementation(libs.androidx.lifecycle.extensions)
  implementation(libs.androidx.lifecycle.viewmodel.ktx)
  implementation(libs.androidx.lifecycle.livedata.ktx)

  implementation(libs.threetenbp)

  implementation(libs.glance.appwidget)
  implementation(libs.glance.material3)
  implementation(libs.glance.preview)
  implementation(libs.glance.appwidget.preview)

  implementation(platform(libs.compose.bom))
  implementation(libs.compose.material3)
  implementation(libs.compose.ui.tooling.preview)

  debugImplementation(libs.glance.preview)
  debugImplementation(libs.glance.appwidget.preview)
}
