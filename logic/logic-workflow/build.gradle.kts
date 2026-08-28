plugins {
  id("reminder.kotlin.jvm")
}

dependencies {
  implementation(project(":core:logging-api"))
  implementation(project(":core:domain"))
  implementation(project(":core:feature-flags-api"))
  implementation(project(":data:repository-api"))
  implementation(project(":data:files-api"))
  implementation(project(":data:work-api"))
  implementation(project(":logic:logic-reminder"))
  implementation(project(":logic:logic-schedule"))

  implementation(libs.koin.core)
  implementation(libs.threetenbp)

  testImplementation(libs.junit)
  testImplementation(libs.mockk)
  testImplementation(libs.kotlinx.coroutines.test)
}
