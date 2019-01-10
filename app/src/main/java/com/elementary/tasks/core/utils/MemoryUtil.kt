package com.elementary.tasks.core.utils

import android.content.ContentResolver
import android.net.Uri
import android.os.Environment
import android.util.Base64
import android.util.Base64InputStream
import android.util.Base64OutputStream
import timber.log.Timber
import java.io.*

import java.util.Locale

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

object MemoryUtil {

    const val DIR_SD = "backup"
    private const val DIR_PREFS = "preferences"
    const val DIR_NOTES_SD = "notes"
    const val DIR_GROUP_SD = "groups"
    const val DIR_BIRTHDAY_SD = "birthdays"
    const val DIR_PLACES_SD = "places"
    const val DIR_TEMPLATES_SD = "templates"
    private const val DIR_MAIL_SD = "mail_attachments"
    private const val DIR_SD_DBX_TMP = "tmp_dropbox"
    private const val DIR_NOTES_SD_DBX_TMP = "tmp_dropbox_notes"
    private const val DIR_GROUP_SD_DBX_TMP = "tmp_dropbox_groups"
    private const val DIR_BIRTHDAY_SD_DBX_TMP = "tmp_dropbox_birthdays"
    private const val DIR_PLACES_SD_DBX_TMP = "tmp_dropbox_places"
    private const val DIR_TEMPLATES_SD_DBX_TMP = "tmp_dropbox_templates"
    private const val DIR_PREFERENCES_SD_DBX_TMP = "tmp_dropbox_preferences"
    private const val DIR_SD_GDRIVE_TMP = "tmp_gdrive"
    private const val DIR_NOTES_SD_GDRIVE_TMP = "tmp_gdrive_notes"
    private const val DIR_GROUP_SD_GDRIVE_TMP = "tmp_gdrive_group"
    private const val DIR_BIRTHDAY_SD_GDRIVE_TMP = "tmp_gdrive_birthdays"
    private const val DIR_PLACES_SD_GDRIVE_TMP = "tmp_gdrive_places"
    private const val DIR_TEMPLATES_SD_GDRIVE_TMP = "tmp_gdrive_templates"
    private const val DIR_PREFERENCES_SD_GDRIVE_TMP = "tmp_gdrive_preferences"

    val isSdPresent: Boolean
        get() {
            val state = Environment.getExternalStorageState()
            return Environment.MEDIA_MOUNTED == state || Environment.MEDIA_MOUNTED_READ_ONLY == state
        }

    val remindersDir: File?
        get() = getDir(DIR_SD)

    val groupsDir: File?
        get() = getDir(DIR_GROUP_SD)

    val birthdaysDir: File?
        get() = getDir(DIR_BIRTHDAY_SD)

    val notesDir: File?
        get() = getDir(DIR_NOTES_SD)

    val placesDir: File?
        get() = getDir(DIR_PLACES_SD)

    val templatesDir: File?
        get() = getDir(DIR_TEMPLATES_SD)

    val googlePlacesDir: File?
        get() = getDir(DIR_PLACES_SD_GDRIVE_TMP)

    val googleTemplatesDir: File?
        get() = getDir(DIR_TEMPLATES_SD_GDRIVE_TMP)

    val dropboxPlacesDir: File?
        get() = getDir(DIR_PLACES_SD_DBX_TMP)

    val dropboxTemplatesDir: File?
        get() = getDir(DIR_TEMPLATES_SD_DBX_TMP)

    val dropboxRemindersDir: File?
        get() = getDir(DIR_SD_DBX_TMP)

    val dropboxGroupsDir: File?
        get() = getDir(DIR_GROUP_SD_DBX_TMP)

    val dropboxBirthdaysDir: File?
        get() = getDir(DIR_BIRTHDAY_SD_DBX_TMP)

    val dropboxNotesDir: File?
        get() = getDir(DIR_NOTES_SD_DBX_TMP)

    val googleRemindersDir: File?
        get() = getDir(DIR_SD_GDRIVE_TMP)

    val googleGroupsDir: File?
        get() = getDir(DIR_GROUP_SD_GDRIVE_TMP)

    val googleBirthdaysDir: File?
        get() = getDir(DIR_BIRTHDAY_SD_GDRIVE_TMP)

    val googleNotesDir: File?
        get() = getDir(DIR_NOTES_SD_GDRIVE_TMP)

    val mailDir: File?
        get() = getDir(DIR_MAIL_SD)

    val prefsDir: File?
        get() = getDir(DIR_PREFS)

    val googlePrefsDir: File?
        get() = getDir(DIR_PREFERENCES_SD_GDRIVE_TMP)

    val dropboxPrefsDir: File?
        get() = getDir(DIR_PREFERENCES_SD_DBX_TMP)

    val parent: File?
        get() = getDir("")

    val imagesDir: File?
        get() = getDir("image_cache")

    private fun getDir(directory: String): File? {
        return if (isSdPresent) {
            val sdPath = Environment.getExternalStorageDirectory()
            val dir = File(sdPath.toString() + "/JustReminder/" + directory)
            if (!dir.exists() && dir.mkdirs()) {
                dir
            } else dir
        } else {
            null
        }
    }

    fun humanReadableByte(bytes: Long, si: Boolean): String {
        val unit = if (si) 1000 else 1024
        if (bytes < unit) {
            return bytes.toString() + " B"
        }
        val exp = (Math.log(bytes.toDouble()) / Math.log(unit.toDouble())).toInt()
        val pre = (if (si) "kMGTPE" else "KMGTPE")[exp - 1] + if (si) "" else ""
        return String.format(Locale.getDefault(), "%.1f %sB", bytes / Math.pow(unit.toDouble(), exp.toDouble()), pre)
    }

    @Throws(IOException::class)
    fun readFileToJson(cr: ContentResolver, name: Uri): String? {
        var inputStream: InputStream? = null
        try {
            inputStream = cr.openInputStream(name)
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: SecurityException) {
            e.printStackTrace()
        }

        if (inputStream == null) {
            return null
        }
        val output64 = Base64InputStream(inputStream, Base64.DEFAULT)
        val r = BufferedReader(InputStreamReader(output64))
        val total = StringBuilder()
        var line: String?
        do {
            line = r.readLine()
            if (line != null) {
                total.append(line)
            }
        } while (line != null)
        output64.close()
        inputStream.close()
        val res = total.toString()
        return if (res.startsWith("{") && res.endsWith("}") || res.startsWith("[") && res.endsWith("]")) {
            Timber.d("readFileToJson: $res")
            res
        } else {
            Timber.d("readFileToJson: Bad JSON")
            throw IOException("Bad JSON")
        }
    }

    @Throws(IOException::class)
    fun readFileToJson(path: String): String {
        try {
            val inputStream = FileInputStream(path)
            val output64 = Base64InputStream(inputStream, Base64.DEFAULT)
            val r = BufferedReader(InputStreamReader(output64))
            val total = StringBuilder()
            var line: String?
            do {
                line = r.readLine()
                if (line != null) {
                    total.append(line)
                }
            } while (line != null)
            output64.close()
            inputStream.close()
            val res = total.toString()
            return if (res.startsWith("{") && res.endsWith("}") || res.startsWith("[") && res.endsWith("]")) {
                Timber.d("readFileToJson: $res")
                res
            } else {
                Timber.d("readFileToJson: Bad JSON")
                throw IOException("Bad JSON")
            }
        } catch (e: Exception) {
            throw IOException("No read permission")
        }
    }

    /**
     * Write data to file.
     *
     * @param file target file.
     * @param data object data.
     * @return Path to file
     * @throws IOException
     */
    @Throws(IOException::class)
    fun writeFile(file: File, data: String?): String? {
        if (data == null) return null
        Timber.d("writeFile: $data")
        try {
            val inputStream = ByteArrayInputStream(data.toByteArray())
            val buffer = ByteArray(8192)
            var bytesRead: Int
            val output = ByteArrayOutputStream()
            val output64 = Base64OutputStream(output, Base64.DEFAULT)
            try {
                do {
                    bytesRead = inputStream.read(buffer)
                    if (bytesRead != -1) {
                        output64.write(buffer, 0, bytesRead)
                    }
                } while (bytesRead != -1)
            } catch (e: IOException) {
                e.printStackTrace()
            }

            output64.close()

            if (file.exists()) {
                file.delete()
            }
            val fw = FileWriter(file)
            fw.write(output.toString())
            fw.close()
            output.close()
        } catch (e: SecurityException) {
            return null
        }
        return file.toString()
    }
}
