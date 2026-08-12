plugins {
  id("reminder.android.library")
  alias(libs.plugins.ksp)
}

android {
  namespace = "com.github.naz013.appfunctions"
}

ksp {
  arg("appfunctions:aggregateAppFunctions", "true")
}

dependencies {
  implementation(project(":domain"))
  implementation(project(":repository-api"))
  implementation(project(":logging-api"))
  implementation(project(":analytics"))
  implementation(project(":platform-common"))
  implementation(project(":date-calculations"))
  implementation(project(":cloud-api"))
  implementation(project(":cloud"))

  implementation(libs.koin.android)
  implementation(libs.threetenbp)

  implementation(libs.androidx.appfunctions)
  ksp(libs.androidx.appfunctions.compiler)

  testImplementation(libs.junit)
  testImplementation(libs.mockk)
  testImplementation(libs.kotlinx.coroutines.test)
}
