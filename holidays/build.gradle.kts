plugins {
  id("reminder.android.library")
}

android {
  namespace = "com.github.naz013.holidays"
}

dependencies {
  implementation(project(":holidays-api"))
  implementation(project(":domain"))
  implementation(project(":repository-api"))
  implementation(project(":work-api"))
  implementation(project(":logging-api"))
  implementation(project(":feature-common"))

  implementation(platform(libs.firebase.bom))
  implementation(libs.firebase.firestore) {
    // Ensure all transitive dependencies are included
    isTransitive = true
  }
  implementation(libs.grpc.okhttp)
  implementation(libs.grpc.android)

  implementation(libs.koin.android)
  implementation(libs.gson)
  implementation(libs.threetenbp)

  testImplementation(libs.junit)
  testImplementation(libs.mockk)
  testImplementation(libs.kotlinx.coroutines.test)
}
