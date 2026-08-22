package com.github.naz013.preferences

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson

/**
 * Generic typed key-value storage over one [SharedPreferences] file, extracted out of `app` so a
 * second app can reuse the storage mechanics without depending on `app`'s specific key/property
 * list. `prefsName` is the caller's own `SharedPreferences` file name - this class has no
 * knowledge of which app or which keys are being stored.
 */
abstract class SharedPrefs(
  protected val context: Context,
  prefsName: String,
) {
  private var prefs: SharedPreferences =
    context.getSharedPreferences(
      prefsName,
      Context.MODE_PRIVATE,
    )

  fun getLongArray(stringToLoad: String): Array<Long> =
    try {
      prefs
        .getStringSet(stringToLoad, setOf<String>())
        ?.map {
          try {
            it.toLong()
          } catch (e: Exception) {
            0L
          }
        }?.toTypedArray() ?: arrayOf()
    } catch (e: Exception) {
      arrayOf()
    }

  fun putLongArray(
    stringToSave: String,
    array: Array<Long>,
  ) {
    prefs.edit().putStringSet(stringToSave, array.map { it.toString() }.toSet()).apply()
  }

  fun putStringArray(
    stringToSave: String,
    array: Array<String>,
  ) {
    prefs.edit().putStringSet(stringToSave, array.toSet()).apply()
  }

  fun getStringArray(stringToLoad: String): Array<String> =
    try {
      prefs.getStringSet(stringToLoad, setOf<String>())?.toTypedArray() ?: arrayOf()
    } catch (e: Exception) {
      arrayOf()
    }

  fun putString(
    stringToSave: String,
    value: String,
  ) {
    prefs.edit().putString(stringToSave, value).apply()
  }

  fun putInt(
    stringToSave: String,
    value: Int,
  ) {
    prefs.edit().putInt(stringToSave, value).apply()
  }

  fun getInt(
    stringToLoad: String,
    def: Int = 0,
  ): Int =
    try {
      prefs.getInt(stringToLoad, def)
    } catch (e: ClassCastException) {
      try {
        Integer.parseInt(prefs.getString(stringToLoad, "$def") ?: "$def")
      } catch (e1: ClassCastException) {
        def
      }
    }

  fun putLong(
    stringToSave: String,
    value: Long,
  ) {
    prefs.edit().putLong(stringToSave, value).apply()
  }

  fun getLong(
    stringToLoad: String,
    def: Long = 0L,
  ): Long =
    try {
      prefs.getLong(stringToLoad, def)
    } catch (e: ClassCastException) {
      java.lang.Long.parseLong(prefs.getString(stringToLoad, "$def") ?: "$def")
    }

  fun putObject(
    key: String,
    obj: Any,
  ) {
    putString(key, Gson().toJson(obj))
  }

  fun getString(
    stringToLoad: String,
    def: String = "",
  ): String = prefs.getString(stringToLoad, def) ?: def

  fun putBoolean(
    stringToSave: String,
    value: Boolean,
  ) {
    prefs.edit().putBoolean(stringToSave, value).apply()
  }

  fun getBoolean(
    stringToLoad: String,
    def: Boolean = false,
  ): Boolean =
    try {
      prefs.getBoolean(stringToLoad, def)
    } catch (e: ClassCastException) {
      java.lang.Boolean.parseBoolean(prefs.getString(stringToLoad, "false"))
    }

  fun saveVersionBoolean(stringToSave: String) {
    prefs.edit().putBoolean(stringToSave, true).apply()
  }

  fun getVersion(stringToLoad: String): Boolean =
    try {
      prefs.getBoolean(stringToLoad, false)
    } catch (e: ClassCastException) {
      java.lang.Boolean.parseBoolean(prefs.getString(stringToLoad, "false"))
    }

  fun all(): Map<String, *> = prefs.all

  fun sharedPrefs(): SharedPreferences = prefs
}
