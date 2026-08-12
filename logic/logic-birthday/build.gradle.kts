plugins {
  id("reminder.kotlin.jvm")
}

dependencies {
  implementation(project(":core:domain"))
  implementation(project(":core:logging-api"))
  implementation(project(":data:repository-api"))
  implementation(project(":core:platform-api"))
  implementation(project(":core:date-calculations"))
  implementation(project(":data:work-api"))
  implementation(project(":data:files-api"))
  implementation(project(":extensions:appwidgets-api"))
  implementation(project(":logic:logic-schedule"))
  implementation(project(":logic:logic-reminder"))

  implementation(libs.koin.core)
  implementation(libs.threetenbp)

  testImplementation(libs.junit)
  testImplementation(libs.mockk)
  testImplementation(libs.kotlinx.coroutines.test)
}
