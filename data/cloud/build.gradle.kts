plugins {
  id("reminder.android.library")
}

android {
  namespace = "com.github.naz013.cloudapi"
}

dependencies {
  implementation(project(":core:domain"))
  implementation(project(":core:logging-api"))
  implementation(project(":data:cloud-api"))
  implementation(project(":data:files-api"))
  implementation(project(":ui:ui-common"))

  implementation(libs.google.api.services.tasks)
  implementation(libs.google.api.services.drive) {
    exclude(group = "org.apache.httpcomponents")
  }
  implementation(libs.google.http.client.gson)
  implementation(libs.google.api.client.android) {
    exclude(group = "org.apache.httpcomponents")
  }

  implementation(libs.play.services.auth)

  implementation(libs.koin.android)
  implementation(libs.koin.android.ext)

  implementation(libs.gson)
  implementation(libs.threetenbp)
  implementation(libs.dropbox.core.sdk)
  implementation(libs.dropbox.android.sdk)
  implementation(libs.okhttp3.logging.interceptor)

  testImplementation(libs.junit)
  testImplementation(libs.mockk)
  testImplementation(libs.kotlinx.coroutines.test)
}
