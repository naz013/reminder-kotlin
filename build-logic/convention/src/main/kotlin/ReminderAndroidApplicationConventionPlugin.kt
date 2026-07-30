package com.github.naz013.buildlogic

import com.android.build.api.dsl.ApplicationExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.KotlinAndroidProjectExtension

class ReminderAndroidApplicationConventionPlugin : Plugin<Project> {
  override fun apply(target: Project) {
    with(target) {
      pluginManager.apply("com.android.application")
      pluginManager.apply("reminder.detekt")
      pluginManager.apply("reminder.kover")

      extensions.configure<ApplicationExtension> {
        compileSdk = catalog.intVersion("compileSdk")

        defaultConfig {
          minSdk = catalog.intVersion("minSdk")
          targetSdk = catalog.intVersion("targetSdk")
          testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        }

        buildTypes {
          release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
          }
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

        compileOptions {
          sourceCompatibility = JavaVersion.VERSION_17
          targetCompatibility = JavaVersion.VERSION_17
        }
      }

      extensions.configure<KotlinAndroidProjectExtension> {
        jvmToolchain(catalog.intVersion("kotlinTargetJvm"))
        compilerOptions {
          applyReminderOptIns()
        }
      }
    }
  }
}
