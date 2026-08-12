package com.github.naz013.feature.common.android

import android.content.Intent
import android.os.Build
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
    // Intents delivered via PendingIntent (notifications, widget clicks) can arrive without the
    // app's classloader attached, which throws ClassNotFoundException reading a custom Parcelable
    // extra on some OS versions (notably Android 11) - set it explicitly before reading.
    setExtrasClassLoader(clazz.classLoader)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
      getParcelableExtra(key, clazz)
    } else {
      getParcelableExtra(key) as? T
    }
  }.getOrNull()
}
