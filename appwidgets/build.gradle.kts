plugins {
  id("reminder.android.library.compose")
}

android {
  namespace = "com.github.naz013.appwidgets"
}

dependencies {
  implementation(project(":domain"))
  implementation(project(":logging-api"))
  implementation(project(":navigation-api"))
  implementation(project(":cloud-api"))
  implementation(project(":platform-common"))
  implementation(project(":feature-common"))
  implementation(project(":feature-note"))
  implementation(project(":ui-common"))
  implementation(project(":usecase:googletasks"))
  implementation(project(":usecase:birthdays"))
  implementation(project(":usecase:notes"))
  implementation(project(":usecase:reminders"))
  implementation(project(":analytics"))
  implementation(project(":icalendar"))
  implementation(project(":date-calculations"))

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
