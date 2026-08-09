import com.android.build.api.dsl.ApplicationExtension
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Properties

plugins {
  id("reminder.android.application.compose")
  alias(libs.plugins.kotlin.parcelize)
  alias(libs.plugins.crashlytics.gradle)
  alias(libs.plugins.google.services)
  alias(libs.plugins.kotlin.serialization)
}

extensions.configure<ApplicationExtension> {
  namespace = "com.elementary.tasks"
  flavorDimensions.add("level")

  defaultConfig {
    applicationId = "com.cray.software.justreminder"
    versionCode = 346
    versionName = "10.0.1"
    multiDexEnabled = true
    renderscriptTargetApi = 23
    renderscriptSupportModeEnabled = true
  }
  buildFeatures {
    viewBinding = true
    buildConfig = true
  }

  val propsFile = file("${rootProject.rootDir}/keystore.properties")
  val props = Properties()
  if (propsFile.exists() && propsFile.canRead()) {
    println("> Property file exist")
    props.load(propsFile.inputStream())
  } else {
    println("> Property file does not exist")
  }

  val shouldSign = props.getProperty("signApk", "false").toBoolean()
  println("> Should sign APK = $shouldSign")

  if (shouldSign) {
    signingConfigs {
      create("freeApp") {
        storeFile = file(props.getProperty("releaseFreeKeyStoreFile"))
        storePassword = props.getProperty("releaseFreeKeyStorePassword")
        keyAlias = props.getProperty("releaseFreeKeyAlias")
        keyPassword = props.getProperty("releaseFreeKeyPassword")
      }
      create("proApp") {
        storeFile = file(props.getProperty("releaseProKeyStoreFile"))
        storePassword = props.getProperty("releaseProKeyStorePassword")
        keyAlias = props.getProperty("releaseProKeyAlias")
        keyPassword = props.getProperty("releaseProKeyPassword")
      }
      create("debugApp") {
        storeFile = file(props.getProperty("debugKeyStoreFile"))
        storePassword = props.getProperty("debugKeyStorePassword")
        keyAlias = props.getProperty("debugKeyAlias")
        keyPassword = props.getProperty("debugKeyPassword")
      }
    }
  }

  productFlavors {
    create("free") {
      dimension = "level"
      applicationId = "com.cray.software.justreminder"
      buildConfigField("boolean", "IS_PRO", "false")

      val api = props.getProperty("freeApiKey", "API_KEY")
      manifestPlaceholders["apiKey"] = api

      buildConfigField("String", "REVIEWS_PROJECT_ID", props.getProperty("reviewsProjectId", "\"\""))
      buildConfigField("String", "REVIEWS_APP_ID", props.getProperty("freeReviewsAppId", "\"\""))
      buildConfigField("String", "REVIEWS_API_KEY", props.getProperty("reviewsApiKey", "\"\""))
      buildConfigField("String", "REVIEWS_STORAGE_BUCKET", props.getProperty("reviewsStorageBucket", "\"\""))
    }
    create("pro") {
      dimension = "level"
      applicationId = "com.cray.software.justreminderpro"
      buildConfigField("boolean", "IS_PRO", "true")

      val api = props.getProperty("proApiKey", "API_KEY")
      manifestPlaceholders["apiKey"] = api

      buildConfigField("String", "REVIEWS_PROJECT_ID", props.getProperty("reviewsProjectId", "\"\""))
      buildConfigField("String", "REVIEWS_APP_ID", props.getProperty("proReviewsAppId", "\"\""))
      buildConfigField("String", "REVIEWS_API_KEY", props.getProperty("reviewsApiKey", "\"\""))
      buildConfigField("String", "REVIEWS_STORAGE_BUCKET", props.getProperty("reviewsStorageBucket", "\"\""))
    }
  }
  packaging {
    resources {
      excludes += "META-INF/INDEX.LIST"
      // pdfbox-android (Bouncy Castle) signing files
      excludes += "META-INF/BCKEY.DSA"
      excludes += "META-INF/BCKEY.SF"
      excludes += "META-INF/BCKEY.RSA"
      excludes += "META-INF/BC2048KE.DSA"
      excludes += "META-INF/BC2048KE.SF"
    }
  }
  buildTypes {
    release {
      buildConfigField("String", "BUILD_DATE", "\"${getDate()}\"")
      isMinifyEnabled = true
      proguardFiles(
        getDefaultProguardFile("proguard-android-optimize.txt"),
        "proguard-rules.pro",
      )
      isDebuggable = false
      isJniDebuggable = false
      if (shouldSign) {
        productFlavors["free"].apply {
          signingConfig = signingConfigs["freeApp"]
        }
        productFlavors["pro"].apply {
          signingConfig = signingConfigs["proApp"]
        }
      }
    }
    debug {
      buildConfigField("String", "BUILD_DATE", "\"${getDateAndTime()}\"")
      isMinifyEnabled = false
      if (shouldSign) {
        signingConfig = signingConfigs["debugApp"]
      }
    }
  }

  @Suppress("UnstableApiUsage")
  testOptions {
    unitTests {
      isReturnDefaultValues = true
      isIncludeAndroidResources = true
    }
  }
  configurations.configureEach {
    resolutionStrategy {
      force("com.google.code.findbugs:jsr305:3.0.2")
    }
    exclude(module = "httpclient")
    exclude(group = "com.google.guava", module = "listenablefuture")
  }
  lint {
    checkReleaseBuilds = false
    abortOnError = false
  }
  composeOptions {
    kotlinCompilerExtensionVersion = libs.versions.kotlinCompilerExtensionVersion.get()
  }
}

kotlin {
  compilerOptions {
    freeCompilerArgs.add("-XXLanguage:+ExplicitBackingFields")
  }
}

configurations.testImplementation {
  exclude(module = "logback-android")
}

configurations.configureEach {
  exclude(group = "com.google.android.gms", module = "play-services-ads")
  exclude(group = "com.google.android.gms", module = "play-services-ads-lite")
}

fun getDateAndTime(): String = DateTimeFormatter.ofPattern("MMMM dd, yyyy hh:mm:ss").format(LocalDateTime.now())

fun getDate(): String = DateTimeFormatter.ofPattern("MMMM dd, yyyy").format(LocalDateTime.now())

dependencies {
  implementation(project(":domain"))

  implementation(project(":logging-api"))
  implementation(project(":repository-api"))
  implementation(project(":scheduler-api"))
  implementation(project(":cloud-api"))
  implementation(project(":work-api"))
  implementation(project(":appwidgets-api"))
  implementation(project(":navigation-api"))
  implementation(project(":icalendar-api"))
  implementation(project(":legal-api"))
  implementation(project(":files-api"))
  implementation(project(":googlecalendar-api"))
  implementation(project(":notification-api"))
  implementation(project(":location-api"))
  implementation(project(":platform-api"))

  implementation(project(":sync"))
  implementation(project(":date-calculations"))
  implementation(project(":logging"))
  implementation(project(":analytics"))
  implementation(project(":repository"))
  implementation(project(":cloud"))
  implementation(project(":work"))
  implementation(project(":appwidgets"))
  implementation(project(":icalendar"))
  implementation(project(":reviews"))
  implementation(project(":legal"))
  implementation(project(":files"))

  implementation(project(":feature-common"))
  implementation(project(":feature-note"))
  implementation(project(":feature-googletask"))
  implementation(project(":feature-reminder"))
  implementation(project(":feature-tags"))
  implementation(project(":feature-insights"))
  implementation(project(":localbackup"))

  implementation(project(":platform-common"))

  implementation(project(":ui-common"))
  implementation(project(":ui-googletask"))
  implementation(project(":ui-reminder"))
  implementation(project(":ui-tag"))

  implementation(project(":usecase:googletasks"))
  implementation(project(":usecase:birthdays"))
  implementation(project(":usecase:notes"))
  implementation(project(":usecase:reminders"))

  implementation(project(":logic-googletask"))
  implementation(project(":logic-reminder"))
  implementation(project(":logic-schedule"))
  implementation(project(":logic-tag"))

  implementation(libs.google.api.services.calendar) {
    exclude(group = "com.google.guava", module = "listenablefuture")
  }
  implementation(libs.google.api.client.android) {
    exclude(group = "org.apache.httpcomponents")
  }

  implementation(libs.koin.android)
  implementation(libs.koin.android.ext)
  implementation(libs.koin.androidx.workmanager)
  implementation(libs.koin.androidx.compose)
  implementation(libs.koin.compose.navigation3)

  implementation(libs.kotlinx.coroutines.core)
  implementation(libs.kotlinx.coroutines.android)

  implementation(libs.material)

  implementation(libs.androidx.multidex)
  implementation(libs.androidx.core.ktx)
  implementation(libs.androidx.activity.compose)

  implementation(libs.androidx.work.runtime) {
    exclude(group = "com.google.guava", module = "listenablefuture")
  }
  implementation(libs.androidx.work.runtime.ktx) {
    exclude(group = "com.google.guava", module = "listenablefuture")
  }

  implementation(libs.androidx.lifecycle.extensions)
  implementation(libs.androidx.lifecycle.viewmodel.ktx)
  implementation(libs.androidx.lifecycle.common.java8)
  implementation(libs.androidx.lifecycle.livedata.ktx)

  implementation(platform(libs.firebase.bom))
  implementation(libs.firebase.crashlytics)
  implementation(libs.firebase.auth)
  implementation(libs.firebase.config)

  implementation(libs.play.services.location)
  implementation(libs.play.services.maps)
  implementation(libs.play.services.auth)
  implementation(libs.maps.compose)

  "freeImplementation"(libs.ads.mobile.sdk)
  "freeImplementation"(libs.user.messaging.platform)

  "proImplementation"(project(":appfunctions"))
  "proImplementation"(libs.install.referrer)

  implementation(libs.retrofit)
  implementation(libs.retrofit.converter.gson)
  implementation(libs.gson)
  implementation(libs.jsr305)
  implementation(libs.okhttp3.logging.interceptor)
  implementation(libs.lib.recur)
  implementation(libs.commons.lang3)
  implementation(libs.lottie)
  implementation(libs.lottie.compose)

  implementation(libs.kotlin.stdlib)
  implementation(libs.kotlin.reflect)

  implementation(libs.threetenbp)
  implementation(libs.coil)
  implementation(libs.coil.compose)
  implementation(libs.telephoto.zoomable.image.coil)
  implementation(libs.pdfbox.android)

  implementation(libs.slf4j.api)
  implementation(libs.logback.android)

  implementation(platform(libs.compose.bom))
  implementation(libs.compose.material3)
  implementation(libs.compose.material.iconsext)
  implementation(libs.compose.runtime.livedata)
  implementation(libs.compose.foundation.foundation)
  implementation(libs.compose.ui.ui)

  debugImplementation(libs.compose.ui.test.manifest)
  implementation(libs.compose.ui.tooling.preview)
  debugImplementation(libs.compose.ui.tooling)

  implementation(libs.androidx.navigation3.runtime)
  implementation(libs.androidx.navigation3.ui)
  implementation(libs.androidx.lifecycle.viewmodel.navigation3)
  implementation(libs.kotlinx.serialization.core)

  testImplementation(project(":testing"))
  testImplementation(libs.junit)
  testImplementation(libs.androidx.test.core)
  testImplementation(libs.mockk)
  testImplementation(libs.mockito.core)
  testImplementation(libs.mockito.kotlin)
  testImplementation(libs.androidx.core.testing)
  testImplementation(libs.androidx.lifecycle.runtime.testing)
  testImplementation(libs.kotlinx.coroutines.test)
  testImplementation(libs.robolectric)
}
