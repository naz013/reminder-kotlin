package com.github.naz013.common

import android.content.Context
import androidx.annotation.ArrayRes
import androidx.annotation.StringRes

class TextProvider(initContext: Context) {

  private val context: Context = initContext

  fun getString(@StringRes id: Int): String {
    return context.getString(id)
  }

  fun getText(@StringRes id: Int): String {
    return getString(id)
  }

  fun getString(@StringRes id: Int, vararg args: Any): String {
    return context.getString(id, *args)
  }

  fun getText(@StringRes id: Int, vararg args: Any): String {
    return getString(id, *args)
  }

  fun getStringArray(@ArrayRes id: Int): Array<String> {
    return context.resources.getStringArray(id)
  }

  fun getAppName(): String {
    val packageManager = context.packageManager
    val applicationInfo = context.applicationInfo
    return packageManager.getApplicationLabel(applicationInfo).toString()
  }
}
