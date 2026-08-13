package com.github.naz013.buildlogic

import org.jetbrains.kotlin.gradle.dsl.KotlinJvmCompilerOptions

fun KotlinJvmCompilerOptions.applyReminderOptIns() {
  freeCompilerArgs.add("-Xreturn-value-checker=full")
  freeCompilerArgs.add("-Xname-based-destructuring=only-syntax")
  freeCompilerArgs.add("-Xcollection-literals")
  freeCompilerArgs.add("-Xcontext-sensitive-resolution")
}
