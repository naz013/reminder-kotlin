plugins {
  id("reminder.android.library")
}

android {
  namespace = "com.github.naz013.digest"
}

dependencies {
  implementation(project(":data:digest-api"))
  implementation(project(":core:domain"))
  implementation(project(":data:repository-api"))
  implementation(project(":data:work-api"))
  implementation(project(":core:logging-api"))
  implementation(project(":core:feature-common"))
  implementation(project(":ui:ui-common"))
  implementation(project(":logic:logic-notification-action"))

  implementation(libs.mlkit.genai.summarization)
  implementation(libs.kotlinx.coroutines.guava)

  implementation(libs.koin.android)
  implementation(libs.threetenbp)

  testImplementation(libs.junit)
  testImplementation(libs.mockk)
  testImplementation(libs.kotlinx.coroutines.test)
}
