plugins {
  id("reminder.kotlin.jvm")
}

dependencies {
  implementation(project(":core:domain"))
  implementation(project(":data:cloud-api"))
  implementation(project(":data:repository-api"))
  implementation(project(":core:logging-api"))
  implementation(project(":data:files-api"))

  implementation(libs.koin.core)
  implementation(libs.threetenbp)
  implementation(libs.kotlinx.coroutines.core)

  testImplementation(libs.junit)
  testImplementation(libs.mockk)
  testImplementation(libs.kotlinx.coroutines.test)
}
