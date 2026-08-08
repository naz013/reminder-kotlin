plugins {
  id("reminder.kotlin.jvm")
}

dependencies {
  implementation(project(":domain"))
  implementation(project(":logging-api"))
  implementation(project(":files-api"))
  implementation(project(":logic-schedule"))
  implementation(project(":date-calculations"))
  implementation(project(":icalendar-api"))
  implementation(project(":repository-api"))
  implementation(project(":appwidgets-api"))
  implementation(project(":scheduler-api"))
  implementation(project(":location-api"))
  implementation(project(":notification-api"))
  implementation(project(":googlecalendar-api"))
  implementation(project(":work-api"))
  implementation(project(":platform-api"))

  implementation(libs.koin.core)
  implementation(libs.threetenbp)

  testImplementation(libs.junit)
  testImplementation(libs.mockk)
  testImplementation(libs.kotlinx.coroutines.test)
}
