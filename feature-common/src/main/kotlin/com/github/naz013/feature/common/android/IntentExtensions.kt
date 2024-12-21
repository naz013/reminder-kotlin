package com.github.naz013.feature.common.android

import android.content.Intent
import android.os.Build
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
