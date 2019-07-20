package com.elementary.tasks.core.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.DocumentsContract
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
        val fileId = DocumentsContract.getDocumentId(uri)
        val file = File(cacheDir, fileId)

        Timber.d("cacheFile: $fileId, ${file.absolutePath}, $inputStream")

        if (hasCache(fileId) && file.exists()) {
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
            saveCache(file.absolutePath)
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