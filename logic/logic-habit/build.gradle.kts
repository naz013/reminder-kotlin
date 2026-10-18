plugins {
  id("reminder.android.library")
}

android {
  namespace = "com.github.naz013.logic.habit"
}

dependencies {
  implementation(project(":core:domain"))
  implementation(project(":core:logging-api"))
  implementation(project(":core:feature-common"))
  implementation(project(":core:date-calculations"))
  implementation(project(":data:repository-api"))
  implementation(project(":data:files-api"))
  implementation(project(":logic:logic-schedule"))

  implementation(libs.threetenbp)
  implementation(libs.koin.core)
  implementation(libs.kotlinx.coroutines.core)

  testImplementation(libs.junit)
  testImplementation(libs.mockk)
  testImplementation(libs.kotlinx.coroutines.test)
  testImplementation(project(":core:testing"))
}
