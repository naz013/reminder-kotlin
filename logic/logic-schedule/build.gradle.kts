plugins {
  id("reminder.kotlin.jvm")
}

dependencies {
  implementation(project(":core:domain"))
  implementation(project(":core:logging-api"))
  implementation(project(":data:work-api"))
  implementation(project(":data:files-api"))
  implementation(project(":data:cloud-api"))
  implementation(project(":data:sync"))

  implementation(libs.koin.core)

  testImplementation(libs.junit)
  testImplementation(libs.mockk)
}
