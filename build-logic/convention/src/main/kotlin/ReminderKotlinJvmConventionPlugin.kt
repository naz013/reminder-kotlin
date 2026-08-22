package com.github.naz013.buildlogic

import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension

class ReminderKotlinJvmConventionPlugin : Plugin<Project> {
  override fun apply(target: Project) {
    with(target) {
      pluginManager.apply("java-library")
      pluginManager.apply("org.jetbrains.kotlin.jvm")
      pluginManager.apply("reminder.detekt")

      extensions.configure<JavaPluginExtension> {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
      }

      extensions.configure<KotlinJvmProjectExtension> {
        jvmToolchain(catalog.intVersion("kotlinTargetJvm"))
        compilerOptions {
          applyReminderOptIns()
        }
      }
    }
  }
}
