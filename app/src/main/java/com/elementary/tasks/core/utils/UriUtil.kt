package com.elementary.tasks.core.utils

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import com.crashlytics.android.Crashlytics
import com.elementary.tasks.BuildConfig
import timber.log.Timber
import java.io.File
import java.util.*

object UriUtil {

    const val URI_MIME = "application/x-arc-uri-list"
    const val ANY_MIME = "any"
    const val IMAGE_MIME = "image/*"

    fun obtainPath(context: Context, uri: Uri, onReady: (Boolean, String?) -> Unit) {
        launchIo {
            try {
                val id = UUID.randomUUID().toString()
                val inputStream = context.contentResolver.openInputStream(uri)
                val dir = MemoryUtil.imagesDir
                if (dir == null || inputStream == null) {
                    withUIContext { onReady.invoke(false, null) }
                } else {
                    val file = File("$dir/$id")
                    Timber.d("obtainPath: $file")
                    if (file.createNewFile()) {
                        file.copyInputStreamToFile(inputStream)
                        val filePath = file.absolutePath
                        withUIContext { onReady.invoke(true, filePath) }
                    } else {
                        withUIContext { onReady.invoke(false, null) }
                    }
                }
            } catch (e: Exception) {
                Crashlytics.logException(e)
                withUIContext { onReady.invoke(false, null) }
            }
        }
    }

    fun getUri(context: Context, filePath: String): Uri? {
        Timber.d("getUri: %s", BuildConfig.APPLICATION_ID)
        return if (Module.isNougat) {
            try {
                FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID + ".provider", File(filePath))
            } catch (e: java.lang.Exception) {
                Crashlytics.logException(e)
                null
            }
        } else {
            Uri.fromFile(File(filePath))
        }
    }

    fun getUri(context: Context, file: File): Uri? {
        Timber.d("getUri: %s", BuildConfig.APPLICATION_ID)
        return if (Module.isNougat) {
            try {
                FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID + ".provider", file)
            } catch (e: java.lang.Exception) {
                Crashlytics.logException(e)
                null
            }
        } else {
            Uri.fromFile(file)
        }
    }
}
