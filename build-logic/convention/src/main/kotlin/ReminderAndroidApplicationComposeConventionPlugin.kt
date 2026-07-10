package com.github.naz013.buildlogic

import com.android.build.api.dsl.ApplicationExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

class ReminderAndroidApplicationComposeConventionPlugin : Plugin<Project> {
  override fun apply(target: Project) {
    with(target) {
      pluginManager.apply("reminder.android.application")
      pluginManager.apply("org.jetbrains.kotlin.plugin.compose")

      extensions.configure<ApplicationExtension> {
        buildFeatures {
          compose = true
        }
      }
    }
  }
}
