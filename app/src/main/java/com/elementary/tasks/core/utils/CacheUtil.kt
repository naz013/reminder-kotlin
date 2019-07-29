package com.elementary.tasks.core.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.provider.DocumentsContract
import android.provider.OpenableColumns
import androidx.annotation.RequiresPermission
import com.elementary.tasks.R
import timber.log.Timber
import java.io.File


class CacheUtil(val context: Context) {

    private val sp = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    @RequiresPermission(Permissions.READ_EXTERNAL)
    fun pickImage(activity: Activity, code: Int) {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        try {
            activity.startActivityForResult(Intent.createChooser(intent, activity.getString(R.string.select_image)), code)
        } catch (e: Exception) {
        }
    }

    @RequiresPermission(Permissions.READ_EXTERNAL)
    fun pickMelody(activity: Activity, code: Int) {
        val intent = Intent()
        intent.type = "audio/*"
        intent.action = Intent.ACTION_GET_CONTENT
        try {
            activity.startActivityForResult(Intent.createChooser(intent, activity.getString(R.string.select_melody)), code)
        } catch (e: Exception) {
        }
    }

    @RequiresPermission(Permissions.READ_EXTERNAL)
    fun pickFile(activity: Activity, code: Int) {
        val intent = Intent()
        intent.action = Intent.ACTION_GET_CONTENT
        try {
            activity.startActivityForResult(Intent.createChooser(intent, activity.getString(R.string.choose_file)), code)
        } catch (e: Exception) {
        }
    }

    fun removeFromCache(path: String) {
        val file = File(path)
        if (file.exists()) {
            file.delete()
            sp.edit().remove(path).apply()
        }
    }

    @RequiresPermission(Permissions.READ_EXTERNAL)
    fun cacheFile(intent: Intent?): String? {
        val uri = intent?.data ?: return null
        return cacheFile(uri)
    }

    @RequiresPermission(Permissions.READ_EXTERNAL)
    fun cacheFile(uri: Uri): String? {
        val cacheDir = context.externalCacheDir ?: context.cacheDir
        val inputStream = context.contentResolver.openInputStream(uri)
        val fileId = try {
            DocumentsContract.getDocumentId(uri)
        } catch (e: Exception) {
            ""
        }

        val cursor: Cursor? = context.contentResolver.query( uri, null, null,
                null, null, null)

        val name = cursor?.use {
            if (it.moveToFirst()) {
                val displayName: String =
                        it.getString(it.getColumnIndex(OpenableColumns.DISPLAY_NAME))
                displayName
            } else {
                ""
            }
        } ?: ""
        if (name.isEmpty() && fileId.isEmpty()) {
            return null
        }

        val fileName = if (name.isEmpty()) fileId else name
        val file = File(cacheDir, fileName)
        val fId = if (fileId.isEmpty()) name else fileId

        Timber.d("cacheFile: $fId, ${file.absolutePath}, $fileName")

        if (hasCache(fId) && file.exists()) {
            Timber.d("cacheFile: FROM CACHE")
            return file.absolutePath
        }

        if (inputStream != null) {
            if (!file.createNewFile()) {
                try {
                    file.delete()
                    file.createNewFile()
                } catch (e: Exception) {
                }
            }
            file.copyInputStreamToFile(inputStream)
            saveCache(fId)
            return file.absolutePath
        }
        return null
    }

    private fun hasCache(path: String): Boolean {
        return sp.getBoolean(path, false)
    }

    private fun saveCache(path: String) {
        sp.edit().putBoolean(path, true).apply()
    }

    companion object {
        private const val PREFS_NAME = "cache_prefs"
    }
}