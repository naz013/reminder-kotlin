plugins {
  id("reminder.android.library")
}

android {
  namespace = "com.github.naz013.wearsync"
}

dependencies {
  implementation(project(":core:domain"))
  implementation(project(":core:logging-api"))
  implementation(project(":core:feature-common"))
  implementation(project(":core:date-calculations"))
  implementation(project(":data:repository-api"))
  implementation(project(":extensions:wearsync-api"))
  implementation(project(":logic:logic-notification-action"))
  implementation(project(":logic:logic-reminder"))

  implementation(libs.play.services.wearable)

  implementation(libs.koin.android)
  implementation(libs.koin.android.ext)

  implementation(libs.kotlinx.coroutines.core)
  implementation(libs.kotlinx.coroutines.android)
  implementation(libs.kotlinx.coroutines.play.services)

  implementation(libs.threetenbp)

  testImplementation(libs.junit)
  testImplementation(libs.mockk)
  testImplementation(libs.kotlinx.coroutines.test)
}
