package com.github.naz013.buildlogic

import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.KotlinAndroidProjectExtension

class ReminderAndroidLibraryConventionPlugin : Plugin<Project> {
  override fun apply(target: Project) {
    with(target) {
      pluginManager.apply("com.android.library")
      pluginManager.apply("reminder.detekt")

      extensions.configure<LibraryExtension> {
        compileSdk = catalog.intVersion("compileSdk")

        defaultConfig {
          minSdk = catalog.intVersion("minSdk")
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

      extensions.configure<KotlinAndroidProjectExtension> {
        jvmToolchain(catalog.intVersion("kotlinTargetJvm"))
        compilerOptions {
          applyReminderOptIns()
        }
      }
    }
  }
}
