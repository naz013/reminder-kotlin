plugins {
  alias(libs.plugins.android.library)
  alias(libs.plugins.kotlin.android)
  alias(libs.plugins.ktlint)
}

android {
  namespace = "com.github.naz013.cloudapi"
  compileSdk = libs.versions.compileSdk.get().toInt()

  defaultConfig {
    minSdk = libs.versions.minSdk.get().toInt()

    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    consumerProguardFiles("consumer-rules.pro")
  }

  buildTypes {
    release {
      isMinifyEnabled = false
      proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
    }
  }
  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
  }
  kotlin {
    jvmToolchain(libs.versions.kotlinTargetJvm.get().toInt())
  }
}

dependencies {
  implementation(project(":domain"))
  implementation(project(":logging-api"))
  implementation(project(":cloud-api"))

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
}

ktlint {
  android = true
  outputColorName.set("RED")
}
