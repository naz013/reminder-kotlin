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
  implementation(project(":core:domain"))
  implementation(project(":data:repository-api"))
  implementation(project(":core:logging-api"))
  implementation(project(":core:analytics"))
  implementation(project(":core:platform-common"))
  implementation(project(":core:date-calculations"))
  implementation(project(":data:cloud-api"))
  implementation(project(":data:cloud"))

  implementation(libs.koin.android)
  implementation(libs.threetenbp)

  implementation(libs.androidx.appfunctions)
  ksp(libs.androidx.appfunctions.compiler)

  testImplementation(libs.junit)
  testImplementation(libs.mockk)
  testImplementation(libs.kotlinx.coroutines.test)
}
