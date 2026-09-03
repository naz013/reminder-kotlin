plugins {
  id("reminder.kotlin.jvm")
}

dependencies {
  implementation(project(":core:domain"))
  implementation(project(":core:logging-api"))
  implementation(project(":core:date-calculations"))
  implementation(project(":data:repository-api"))
  implementation(project(":logic:logic-reminder"))
  implementation(project(":logic:logic-birthday"))
  implementation(project(":logic:logic-note"))

  implementation(libs.koin.core)
  implementation(libs.threetenbp)

  testImplementation(libs.junit)
  testImplementation(libs.mockk)
  testImplementation(libs.kotlinx.coroutines.test)
}
