import com.android.build.api.dsl.LibraryExtension
import org.gradle.kotlin.dsl.configure

plugins {
  alias(libs.plugins.android.library)
  alias(libs.plugins.ktlint)
}

extensions.configure<LibraryExtension> {
  namespace = "com.github.naz013.logging"
  compileSdk = libs.versions.compileSdk.get().toInt()

  defaultConfig {
    minSdk = libs.versions.minSdk.get().toInt()

    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    consumerProguardFiles("consumer-rules.pro")
  }

  buildTypes {
    release {
      isMinifyEnabled = false
      proguardFiles(
        getDefaultProguardFile("proguard-android-optimize.txt"),
        "proguard-rules.pro"
      )
    }
  }
  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
  }
}

kotlin {
  jvmToolchain(libs.versions.kotlinTargetJvm.get().toInt())
}

dependencies {
  implementation(project(":logging-api"))
  implementation(libs.slf4j.api)
  implementation(libs.logback.android)

  implementation(platform(libs.firebase.bom))
  implementation(libs.firebase.crashlytics)

  testImplementation(libs.logback.classic)
}

ktlint {
  android = true
  outputColorName.set("RED")
}
