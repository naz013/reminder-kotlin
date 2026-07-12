package com.github.naz013.buildlogic

import org.jetbrains.kotlin.gradle.dsl.KotlinJvmCompilerOptions

fun KotlinJvmCompilerOptions.applyReminderOptIns() {
  optIn.add("-Xreturn-value-checker=check")
  optIn.add("-Xexplicit-backing-fields")
  optIn.add("-Xname-based-destructuring=only-syntax")
  optIn.add("-Xdata-flow-based-exhaustiveness")
  optIn.add("-Xcollection-literals")

  freeCompilerArgs.add("-Xcontext-parameters")
}
