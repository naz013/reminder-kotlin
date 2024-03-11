package com.elementary.tasks.core.os

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import java.io.Serializable

fun <T : Serializable> Intent.readSerializable(key: String, clazz: Class<T>): T? {
  return runCatching {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
      getSerializableExtra(key, clazz)
    } else {
      getSerializableExtra(key) as? T
    }
  }.getOrNull()
}

fun <T : Parcelable> Intent.readParcelable(key: String, clazz: Class<T>): T? {
  return runCatching {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
      getParcelableExtra(key, clazz)
    } else {
      getParcelableExtra(key) as? T
    }
  }.getOrNull()
}

fun <T : Parcelable> Bundle.readParcelable(key: String, clazz: Class<T>): T? {
  return runCatching {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
      getParcelable(key, clazz)
    } else {
      getParcelable(key) as? T
    }
  }.getOrNull()
}
