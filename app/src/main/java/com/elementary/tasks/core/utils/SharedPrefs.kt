package com.elementary.tasks.core.utils

import android.content.Context
import android.content.SharedPreferences
import com.elementary.tasks.core.cloud.FileConfig
import com.google.gson.Gson
import java.io.*

/**
 * Copyright 2016 Nazar Suhovich
 *
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

abstract class SharedPrefs(context: Context) {
    private var prefs: SharedPreferences = context.getSharedPreferences(PrefsConstants.PREFS_NAME, Context.MODE_PRIVATE)

    fun putString(stringToSave: String, value: String) {
        prefs.edit().putString(stringToSave, value).apply()
    }

    fun putInt(stringToSave: String, value: Int) {
        prefs.edit().putInt(stringToSave, value).apply()
    }

    fun getInt(stringToLoad: String): Int {
        return try {
            prefs.getInt(stringToLoad, 0)
        } catch (e: ClassCastException) {
            try {
                Integer.parseInt(prefs.getString(stringToLoad, "0"))
            } catch (e1: ClassCastException) {
                0
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
            java.lang.Long.parseLong(prefs.getString(stringToLoad, "1000"))
        }
    }

    fun putObject(key: String, obj: Any) {
        putString(key, Gson().toJson(obj))
    }

    fun getString(stringToLoad: String): String {
        return prefs.getString(stringToLoad, "")
    }

    fun hasKey(checkString: String): Boolean {
        return prefs.contains(checkString)
    }

    fun putBoolean(stringToSave: String, value: Boolean) {
        prefs.edit().putBoolean(stringToSave, value).apply()
    }

    fun getBoolean(stringToLoad: String): Boolean {
        return try {
            prefs.getBoolean(stringToLoad, false)
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
                prefEdit.clear()
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