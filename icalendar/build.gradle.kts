plugins {
  alias(libs.plugins.android.library)
  alias(libs.plugins.kotlin.android)
  alias(libs.plugins.ktlint)
}

android {
  namespace = "com.github.naz013.icalendar"
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
  kotlinOptions {
    jvmTarget = libs.versions.kotlinTargetJvm.get()
  }
  sourceSets["main"].java {
    srcDir("src/main/kotlin")
  }
}

dependencies {
  implementation(project(":domain"))
  implementation(project(":logging-api"))

  implementation(libs.koin.android)

  implementation(libs.gson)
  implementation(libs.threetenbp)
  implementation(libs.lib.recur)

  testImplementation(libs.junit)
  testImplementation(libs.mockk)
  testImplementation(libs.mockito.core)
  testImplementation(libs.mockito.kotlin)
}

ktlint {
  android = true
  outputColorName.set("RED")
}
