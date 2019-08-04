package com.elementary.tasks.core.utils

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Environment
import android.provider.OpenableColumns
import android.util.Base64
import android.util.Base64InputStream
import android.util.Base64OutputStream
import com.elementary.tasks.core.cloud.FileConfig
import com.elementary.tasks.core.data.models.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import timber.log.Timber
import java.io.*
import java.nio.charset.StandardCharsets
import java.util.*
import kotlin.math.ln
import kotlin.math.pow


object MemoryUtil {

    private const val DIR_SD = "backup"
    private const val DIR_PREFS = "preferences"
    private const val DIR_NOTES_SD = "notes"
    private const val DIR_GROUP_SD = "groups"
    private const val DIR_BIRTHDAY_SD = "birthdays"
    private const val DIR_PLACES_SD = "places"
    private const val DIR_TEMPLATES_SD = "templates"

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

    val prefsDir: File?
        get() = getDir(DIR_PREFS)

    val parent: File?
        get() = getDir("")

    val imagesDir: File?
        get() = getDir("image_cache")

    private fun getDir(directory: String): File? {
        return if (isSdPresent) {
            val sdPath = Environment.getExternalStorageDirectory()
            val dir = File("$sdPath/JustReminder/$directory")
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
            return "$bytes B"
        }
        val exp = (ln(bytes.toDouble()) / ln(unit.toDouble())).toInt()
        val pre = (if (si) "kMGTPE" else "KMGTPE")[exp - 1] + if (si) "" else ""
        return String.format(Locale.US, "%.1f %sB", bytes / unit.toDouble().pow(exp.toDouble()), pre)
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

    fun readFileContent(file: File): String? {
        try {
            val inputStream = FileInputStream(file)
            val r = BufferedReader(InputStreamReader(inputStream))
            val total = StringBuilder()
            var line: String?
            do {
                line = r.readLine()
                if (line != null) {
                    total.append(line)
                }
            } while (line != null)
            inputStream.close()
            return total.toString()
        } catch (e: Exception) {
            Timber.d("readFileContent: ${e.message}")
            return null
        }
    }

    @Throws(IOException::class)
    fun writeFileNoEncryption(file: File, data: String?): String? {
        if (data == null) return null
        try {
            val inputStream = ByteArrayInputStream(data.toByteArray())
            val buffer = ByteArray(8192)
            var bytesRead: Int
            val output = ByteArrayOutputStream()
            try {
                do {
                    bytesRead = inputStream.read(buffer)
                    if (bytesRead != -1) {
                        output.write(buffer, 0, bytesRead)
                    }
                } while (bytesRead != -1)
            } catch (e: IOException) {
                e.printStackTrace()
            }

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

    fun encryptJson(data: String?): String? {
        if (data == null) return null
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

            val encrypted = output.toString()
            output.close()
            return encrypted
        } catch (e: SecurityException) {
            return null
        }
    }

    fun decryptToJson(encrypted: String?): String? {
        if (encrypted == null) return null
        return try {
            val inputStream = ByteArrayInputStream(encrypted.toByteArray())
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
            if (res.startsWith("{") && res.endsWith("}") || res.startsWith("[") && res.endsWith("]")) {
                Timber.d("readFileToJson: $res")
                res
            } else {
                Timber.d("readFileToJson: Bad JSON")
                null
            }
        } catch (e: Exception) {
            Timber.d("readFileToJson: Bad JSON")
            null
        }
    }

    fun toStream(any: Any, outputStream: OutputStream): Boolean {
        try {
            val output64 = Base64OutputStream(outputStream, Base64.DEFAULT)
            val bufferedWriter = BufferedWriter(OutputStreamWriter(output64, StandardCharsets.UTF_8))
            val writer = JsonWriter(bufferedWriter)
            val type = when (any) {
                is Reminder -> object : TypeToken<Reminder>() {}.type
                is Place -> object : TypeToken<Place>() {}.type
                is Birthday -> object : TypeToken<Birthday>() {}.type
                is ReminderGroup -> object : TypeToken<ReminderGroup>() {}.type
                is SmsTemplate -> object : TypeToken<SmsTemplate>() {}.type
                is NoteWithImages -> object : TypeToken<OldNote>() {}.type
                else -> null
            } ?: return false
            Timber.d("toStream: $type, $any")
            Gson().toJson(if (any is NoteWithImages) OldNote(any) else any, type, writer)
            writer.close()
            return true
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }

    fun decryptToJson(context: Context, uri: Uri, source: String = ""): Any? {
        val cr = context.contentResolver ?: return null
        var inputStream: InputStream? = null
        try {
            inputStream = cr.openInputStream(uri)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        if (inputStream == null) {
            return null
        }
        val cursor: Cursor? = cr.query(uri, null, null,
                null, null, null)

        val name = cursor?.use {
            if (it.moveToFirst()) {
                val displayName: String =
                        it.getString(it.getColumnIndex(OpenableColumns.DISPLAY_NAME))
                displayName
            } else {
                source
            }
        } ?: source
        Timber.d("decryptToJson: $name, $source")
        return try {
            val output64 = Base64InputStream(inputStream, Base64.DEFAULT)
            val r = JsonReader(BufferedReader(InputStreamReader(output64)))
            val type = when {
                name.endsWith(FileConfig.FILE_NAME_PLACE) -> {
                    object : TypeToken<Place>() {}.type
                }
                name.endsWith(FileConfig.FILE_NAME_REMINDER) -> {
                    object : TypeToken<Reminder>() {}.type
                }
                name.endsWith(FileConfig.FILE_NAME_BIRTHDAY) -> {
                    object : TypeToken<Birthday>() {}.type
                }
                name.endsWith(FileConfig.FILE_NAME_GROUP) -> {
                    object : TypeToken<ReminderGroup>() {}.type
                }
                name.endsWith(FileConfig.FILE_NAME_NOTE) -> {
                    object : TypeToken<OldNote>() {}.type
                }
                name.endsWith(FileConfig.FILE_NAME_TEMPLATE) -> {
                    object : TypeToken<SmsTemplate>() {}.type
                }
                else -> null
            }
            Gson().fromJson(r, type)
        } catch (e: Exception) {
            Timber.d("decryptToJson: Bad JSON")
            e.printStackTrace()
            null
        }
    }
}
