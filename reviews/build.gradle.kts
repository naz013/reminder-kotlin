plugins {
  id("reminder.android.library.compose")
}

android {
  namespace = "com.github.naz013.reviews"

  packaging {
    resources {
      excludes += "META-INF/DEPENDENCIES"
      excludes += "META-INF/LICENSE"
      excludes += "META-INF/LICENSE.txt"
      excludes += "META-INF/NOTICE"
      excludes += "META-INF/NOTICE.txt"
      excludes += "META-INF/INDEX.LIST"
      excludes += "META-INF/io.netty.versions.properties"
    }
  }
}

dependencies {
  implementation(project(":logging-api"))
  implementation(project(":ui-common"))
  implementation(project(":feature-common"))
  implementation(project(":platform-common"))

  implementation(platform(libs.firebase.bom))
  implementation(libs.firebase.crashlytics)
  implementation(libs.firebase.firestore) {
    // Ensure all transitive dependencies are included
    isTransitive = true
  }
  implementation(libs.grpc.okhttp)
  implementation(libs.grpc.android)
  implementation(libs.firebase.auth)
  implementation(libs.firebase.appcheck)
  implementation(libs.firebase.appcheck.playintegrity)
  implementation(libs.firebase.storage)

  implementation(libs.koin.android)
  implementation(libs.koin.android.ext)
  implementation(libs.koin.androidx.compose)

  implementation(libs.play.review.ktx)

  implementation(libs.gson)
  implementation(libs.threetenbp)

  implementation(libs.material)

  implementation(platform(libs.compose.bom))
  implementation(libs.compose.material3)
  implementation(libs.compose.material.iconsext)
  implementation(libs.compose.runtime.livedata)
  implementation(libs.compose.foundation.foundation)
  implementation(libs.compose.ui.ui)
  implementation(libs.compose.ui.tooling.preview)

  debugImplementation(libs.compose.ui.test.manifest)
  debugImplementation(libs.compose.ui.tooling)

  testImplementation(libs.junit)
  testImplementation(libs.mockk)
  testImplementation(libs.kotlinx.coroutines.test)
  testImplementation(libs.androidx.core.testing)
  testImplementation(libs.kotlinx.coroutines.android)
  testImplementation(libs.mockk.android)
}
