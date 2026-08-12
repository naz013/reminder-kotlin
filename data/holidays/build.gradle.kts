plugins {
  id("reminder.android.library")
}

android {
  namespace = "com.github.naz013.holidays"
}

dependencies {
  implementation(project(":data:holidays-api"))
  implementation(project(":core:domain"))
  implementation(project(":data:repository-api"))
  implementation(project(":data:work-api"))
  implementation(project(":core:logging-api"))
  implementation(project(":core:feature-common"))

  implementation(platform(libs.firebase.bom))
  implementation(libs.firebase.firestore) {
    // Ensure all transitive dependencies are included
    isTransitive = true
  }
  implementation(libs.grpc.okhttp)
  implementation(libs.grpc.android)

  implementation(libs.koin.android)
  implementation(libs.threetenbp)

  testImplementation(libs.junit)
  testImplementation(libs.mockk)
  testImplementation(libs.kotlinx.coroutines.test)
}
