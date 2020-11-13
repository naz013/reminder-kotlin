package com.elementary.tasks.core.utils

import android.content.Context
import android.content.SharedPreferences
import com.elementary.tasks.core.cloud.FileConfig
import com.google.gson.Gson
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.io.ObjectInputStream
import java.io.ObjectOutputStream

abstract class SharedPrefs(protected val context: Context) {
  private var prefs: SharedPreferences = context.getSharedPreferences(PrefsConstants.PREFS_NAME, Context.MODE_PRIVATE)

  fun getLongArray(stringToLoad: String): Array<Long> {
    return try {
      prefs.getStringSet(stringToLoad, setOf<String>())?.map {
        try {
          it.toLong()
        } catch (e: Exception) {
          0L
        }
      }?.toTypedArray() ?: arrayOf()
    } catch (e: Exception) {
      arrayOf()
    }
  }

  fun putLongArray(stringToSave: String, array: Array<Long>) {
    prefs.edit().putStringSet(stringToSave, array.map { it.toString() }.toSet()).apply()
  }

  fun putStringArray(stringToSave: String, array: Array<String>) {
    prefs.edit().putStringSet(stringToSave, array.toSet()).apply()
  }

  fun getStringArray(stringToLoad: String): Array<String> {
    return try {
      prefs.getStringSet(stringToLoad, setOf<String>())?.toTypedArray() ?: arrayOf()
    } catch (e: Exception) {
      arrayOf()
    }
  }

  fun putString(stringToSave: String, value: String) {
    prefs.edit().putString(stringToSave, value).apply()
  }

  fun putInt(stringToSave: String, value: Int) {
    prefs.edit().putInt(stringToSave, value).apply()
  }

  fun getInt(stringToLoad: String, def: Int = 0): Int {
    return try {
      prefs.getInt(stringToLoad, def)
    } catch (e: ClassCastException) {
      try {
        Integer.parseInt(prefs.getString(stringToLoad, "$def") ?: "$def")
      } catch (e1: ClassCastException) {
        def
      }
    }
  }

  fun putLong(stringToSave: String, value: Long) {
    prefs.edit().putLong(stringToSave, value).apply()
  }

  fun getLong(stringToLoad: String): Long {
    return try {
      prefs.getLong(stringToLoad, 1000)
    } catch (e: ClassCastException) {
      java.lang.Long.parseLong(prefs.getString(stringToLoad, "1000") ?: "1000")
    }
  }

  fun putObject(key: String, obj: Any) {
    putString(key, Gson().toJson(obj))
  }

  fun getString(stringToLoad: String): String {
    return prefs.getString(stringToLoad, "") ?: ""
  }

  fun hasKey(checkString: String): Boolean {
    return prefs.contains(checkString)
  }

  fun removeKey(checkString: String) {
    prefs.edit().remove(checkString).apply()
  }

  fun putBoolean(stringToSave: String, value: Boolean) {
    prefs.edit().putBoolean(stringToSave, value).apply()
  }

  fun getBoolean(stringToLoad: String, def: Boolean = false): Boolean {
    return try {
      prefs.getBoolean(stringToLoad, def)
    } catch (e: ClassCastException) {
      java.lang.Boolean.parseBoolean(prefs.getString(stringToLoad, "false"))
    }
  }

  fun saveVersionBoolean(stringToSave: String) {
    prefs.edit().putBoolean(stringToSave, true).apply()
  }

  fun getVersion(stringToLoad: String): Boolean {
    return try {
      prefs.getBoolean(stringToLoad, false)
    } catch (e: ClassCastException) {
      java.lang.Boolean.parseBoolean(prefs.getString(stringToLoad, "false"))
    }
  }

  fun all(): Map<String, *> {
    return prefs.all
  }

  fun sharedPrefs(): SharedPreferences {
    return prefs
  }

  fun savePrefsBackup() {
    val dir = MemoryUtil.prefsDir
    if (dir != null) {
      val prefsFile = File("$dir/" + FileConfig.FILE_NAME_SETTINGS)
      if (prefsFile.exists()) {
        prefsFile.delete()
      }
      var output: ObjectOutputStream? = null
      try {
        output = ObjectOutputStream(FileOutputStream(prefsFile))
        val list = prefs.all
        if (list.containsKey(PrefsConstants.DRIVE_USER)) {
          list.remove(PrefsConstants.DRIVE_USER)
        }
        if (list.containsKey(PrefsConstants.TASKS_USER)) {
          list.remove(PrefsConstants.TASKS_USER)
        }
        output.writeObject(list)
      } catch (e: IOException) {
        e.printStackTrace()
      } finally {
        try {
          if (output != null) {
            output.flush()
            output.close()
          }
        } catch (ex: IOException) {
          ex.printStackTrace()
        }
      }
    }
  }

  fun loadPrefsFromFile() {
    val dir = MemoryUtil.prefsDir ?: return
    val prefsFile = File("$dir/" + FileConfig.FILE_NAME_SETTINGS)
    if (prefsFile.exists()) {
      var input: ObjectInputStream? = null
      try {
        input = ObjectInputStream(FileInputStream(prefsFile))
        val prefEdit = prefs.edit()
        val entries = input.readObject() as Map<String, *>
        for ((key, v) in entries) {
          when (v) {
            is Boolean -> prefEdit.putBoolean(key, v)
            is Float -> prefEdit.putFloat(key, v)
            is Int -> prefEdit.putInt(key, v)
            is Long -> prefEdit.putLong(key, v)
            is String -> prefEdit.putString(key, v)
          }
        }
        prefEdit.apply()
      } catch (e: ClassNotFoundException) {
        e.printStackTrace()
      } catch (e: IOException) {
        e.printStackTrace()
      } finally {
        try {
          input?.close()
        } catch (ex: IOException) {
          ex.printStackTrace()
        }

      }
    }
  }
}