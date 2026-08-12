plugins {
  id("reminder.kotlin.jvm")
}

dependencies {
  implementation(project(":core:domain"))
  implementation(project(":core:logging-api"))
  implementation(project(":data:files-api"))
  implementation(project(":logic:logic-schedule"))
  implementation(project(":core:date-calculations"))
  implementation(project(":data:icalendar-api"))
  implementation(project(":data:repository-api"))
  implementation(project(":extensions:appwidgets-api"))
  implementation(project(":data:scheduler-api"))
  implementation(project(":data:location-api"))
  implementation(project(":data:notification-api"))
  implementation(project(":data:googlecalendar-api"))
  implementation(project(":data:work-api"))
  implementation(project(":core:platform-api"))

  implementation(libs.koin.core)
  implementation(libs.threetenbp)

  testImplementation(libs.junit)
  testImplementation(libs.mockk)
  testImplementation(libs.kotlinx.coroutines.test)
}
