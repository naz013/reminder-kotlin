package com.github.naz013.feature.common.android

import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import java.io.Serializable

fun <T : Serializable> Bundle.readSerializable(key: String, clazz: Class<T>): T? {
  return runCatching {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
      getSerializable(key, clazz)
    } else {
      getSerializable(key) as? T
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
