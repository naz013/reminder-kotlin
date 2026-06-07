import com.android.build.api.dsl.LibraryExtension
import org.gradle.kotlin.dsl.configure

plugins {
  alias(libs.plugins.android.library)
  alias(libs.plugins.ktlint)
  alias(libs.plugins.kotlin.parcelize)
}

extensions.configure<LibraryExtension> {
  namespace = "com.github.naz013.navigation"
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
}

kotlin {
  jvmToolchain(libs.versions.kotlinTargetJvm.get().toInt())
}

dependencies {
  implementation(project(":logging-api"))
  implementation(libs.koin.android)

  testImplementation(libs.junit)
  testImplementation(libs.androidx.test.core)
}

ktlint {
  android = true
  outputColorName.set("RED")
}
