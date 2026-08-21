plugins {
  id("reminder.kotlin.jvm")
}

dependencies {
  implementation(project(":core:domain"))
  implementation(project(":core:logging-api"))
  implementation(project(":core:feature-flags-api"))
  implementation(project(":core:date-calculations"))
  implementation(project(":data:repository-api"))
  implementation(project(":data:files-api"))
  implementation(project(":logic:logic-schedule"))

  implementation(libs.threetenbp)
  implementation(libs.koin.core)

  testImplementation(libs.junit)
  testImplementation(libs.mockk)
  testImplementation(libs.kotlinx.coroutines.test)
}
