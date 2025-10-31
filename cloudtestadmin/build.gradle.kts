import java.util.Properties

plugins {
  alias(libs.plugins.android.application)
  alias(libs.plugins.kotlin.android)
  alias(libs.plugins.compose.compiler)
  alias(libs.plugins.google.services)
  alias(libs.plugins.crashlytics.gradle)
}

android {
  namespace = "com.example.cloudtestadmin"
  compileSdk {
    version = release(libs.versions.compileSdk.get().toInt())
  }

  defaultConfig {
    applicationId = "com.cray.software.justreminderpro"
    minSdk = libs.versions.minSdk.get().toInt()
    targetSdk = libs.versions.targetSdk.get().toInt()
    versionCode = 1
    versionName = "1.0"

    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
  }

  val propsFile = file("${rootProject.rootDir}/keystore.properties")
  val props = Properties()
  if (propsFile.exists() && propsFile.canRead()) {
    println("> Property file exist")
    props.load(propsFile.inputStream())
  } else {
    println("> Property file does not exist")
  }

  val shouldSign = props.getProperty("signApk").toBoolean()
  println("> Should sign APK = $shouldSign")

  if (shouldSign) {
    signingConfigs {
      create("debugApp") {
        storeFile = file(props.getProperty("debugKeyStoreFile"))
        storePassword = props.getProperty("debugKeyStorePassword")
        keyAlias = props.getProperty("debugKeyAlias")
        keyPassword = props.getProperty("debugKeyPassword")
      }
    }
  }

  buildTypes {
    release {
      isMinifyEnabled = false
      proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
    }
    debug {
      signingConfig = signingConfigs["debugApp"]
    }
  }
  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
  }
  kotlin {
    jvmToolchain(libs.versions.kotlinTargetJvm.get().toInt())
  }
  buildFeatures {
    compose = true
  }
  packaging {
    resources {
      excludes += "META-INF/NOTICE"
      excludes += "META-INF/LICENSE.txt"
      excludes += "META-INF/NOTICE.txt"
      excludes += "META-INF/proguard/androidx-annotations.pro"
      excludes += "META-INF/DEPENDENCIES"
      excludes += "META-INF/LICENSE"
      excludes += "META-INF/license.txt"
      excludes += "META-INF/ASL2.0"
      excludes += "META-INF/LICENSE.md"
    }
  }
}

dependencies {
  implementation(project(":domain"))
  implementation(project(":logging-api"))
  implementation(project(":logging"))
  implementation(project(":cloud-api"))
  implementation(project(":cloud"))
  implementation(project(":feature-common"))
  implementation(project(":platform-common"))
  implementation(project(":ui-common"))
  implementation(project(":sync"))

  implementation(libs.koin.android)
  implementation(libs.koin.android.ext)
  implementation(libs.koin.androidx.compose)

  implementation(libs.kotlinx.coroutines.core)
  implementation(libs.kotlinx.coroutines.android)

  implementation(libs.androidx.lifecycle.extensions)
  implementation(libs.androidx.lifecycle.viewmodel.ktx)
  implementation(libs.androidx.lifecycle.common.java8)
  implementation(libs.androidx.lifecycle.livedata.ktx)

  implementation(libs.play.services.auth)

  implementation(libs.gson)

  implementation(libs.androidx.activity.compose)

  implementation(platform(libs.compose.bom))
  implementation(libs.compose.material3)
  implementation(libs.compose.material.iconsext)
  implementation(libs.compose.runtime.livedata)
  implementation(libs.compose.foundation.foundation)
  implementation(libs.compose.ui.ui)

  debugImplementation(libs.compose.ui.test.manifest)
  debugImplementation(libs.compose.ui.tooling.preview)

  testImplementation(libs.junit)
  testImplementation(libs.androidx.test.core)
  testImplementation(libs.mockk)
  testImplementation(libs.robolectric)
}
