import com.android.build.api.dsl.LibraryExtension
import org.gradle.kotlin.dsl.configure

plugins {
  alias(libs.plugins.android.library)
  alias(libs.plugins.ktlint)
}

extensions.configure<LibraryExtension> {
  namespace = "com.github.naz013.legal"
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
  compilerOptions {
    optIn.add("-Xreturn-value-checker=check")
    optIn.add("-Xexplicit-backing-fields")
    optIn.add("-Xname-based-destructuring=only-syntax")
    optIn.add("-Xdata-flow-based-exhaustiveness")
    optIn.add("-Xcollection-literals")
  }
}

dependencies {
  implementation(project(":domain"))
  implementation(project(":logging-api"))
  implementation(project(":legal-api"))

  implementation(platform(libs.firebase.bom))
  implementation(libs.firebase.config)

  implementation(libs.koin.android)
  implementation(libs.koin.android.ext)

  implementation(libs.kotlinx.coroutines.core)
  implementation(libs.gson)

  testImplementation(libs.junit)
  testImplementation(libs.mockk)
  testImplementation(libs.kotlinx.coroutines.test)
}

ktlint {
  android = true
  outputColorName.set("RED")
}
