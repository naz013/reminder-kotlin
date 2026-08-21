plugins {
  id("reminder.android.library")
}

android {
  namespace = "com.github.naz013.logic.notificationaction"
}

dependencies {
  implementation(project(":core:domain"))
  implementation(project(":core:logging-api"))
  implementation(project(":core:feature-common"))
  implementation(project(":core:platform-common"))
  implementation(project(":core:analytics"))
  implementation(project(":core:date-calculations"))
  implementation(project(":data:repository-api"))
  implementation(project(":data:scheduler-api"))
  implementation(project(":ui:ui-common"))
  implementation(project(":logic:logic-reminder"))
  implementation(project(":logic:logic-workflow"))

  implementation(libs.androidx.core.ktx)
  implementation(libs.koin.core)
  implementation(libs.threetenbp)

  testImplementation(libs.junit)
  testImplementation(libs.mockk)
  testImplementation(libs.kotlinx.coroutines.test)
  testImplementation(project(":core:testing"))
}
