import com.android.build.api.dsl.ApplicationExtension
import java.util.Properties

plugins {
  id("reminder.android.application.compose")
}

extensions.configure<ApplicationExtension> {
  namespace = "com.github.nsy.reviewsadmin"

  defaultConfig {
    applicationId = "com.github.nsy.reviewsadmin"
    versionCode = 1
    versionName = "1.0"
  }

  val propsFile = file("${rootProject.rootDir}/keystore.properties")
  val props = Properties()
  if (propsFile.exists() && propsFile.canRead()) {
    println("> Property file exist")
    props.load(propsFile.inputStream())
  } else {
    println("> Property file does not exist")
  }

  val debugKeyStoreFile = props.getProperty("reviewsAdminKeyStoreFile")
  val debugKeyStoreFileExists = debugKeyStoreFile?.let { file(it).exists() } ?: false
  println("> Debug keystore file exists = $debugKeyStoreFileExists")

  val shouldSign = debugKeyStoreFileExists
  println("> Should sign APK = $shouldSign")

  if (shouldSign) {
    signingConfigs {
      create("debugApp") {
        storeFile = file(props.getProperty("reviewsAdminKeyStoreFile"))
        storePassword = props.getProperty("reviewsAdminKeyStorePassword")
        keyAlias = props.getProperty("reviewsAdminKeyAlias")
        keyPassword = props.getProperty("reviewsAdminKeyPassword")
      }
    }
  }

  buildTypes {
    debug {
      if (shouldSign) {
        signingConfig = signingConfigs.getByName("debugApp")
      }
      buildConfigField("String", "REVIEWS_PROJECT_ID", props.getProperty("reviewsProjectId", "\"\""))
      buildConfigField("String", "REVIEWS_APP_ID", props.getProperty("reviewsAdminAppId", "\"\""))
      buildConfigField("String", "REVIEWS_API_KEY", props.getProperty("reviewsApiKey", "\"\""))
      buildConfigField("String", "REVIEWS_STORAGE_BUCKET", props.getProperty("reviewsStorageBucket", "\"\""))
    }
  }

  buildFeatures {
    buildConfig = true
  }
}

dependencies {
  implementation(project(":logging-api"))
  implementation(project(":logging"))
  implementation(project(":feature-common"))
  implementation(project(":ui-common"))
  implementation(project(":reviews"))

  implementation(platform(libs.firebase.bom))
  implementation(libs.firebase.auth)

  implementation(libs.koin.android)
  implementation(libs.koin.android.ext)
  implementation(libs.koin.androidx.compose)

  implementation(libs.kotlinx.coroutines.core)
  implementation(libs.kotlinx.coroutines.android)

  implementation(libs.androidx.lifecycle.extensions)
  implementation(libs.androidx.lifecycle.viewmodel.ktx)
  implementation(libs.androidx.lifecycle.common.java8)
  implementation(libs.androidx.lifecycle.livedata.ktx)

  implementation(libs.gson)
  implementation(libs.threetenbp)

  implementation(libs.androidx.activity.compose)

  implementation(libs.material)

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
