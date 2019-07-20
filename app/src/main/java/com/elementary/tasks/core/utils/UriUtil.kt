package com.elementary.tasks.core.utils

import android.content.Context
import android.net.Uri
import androidx.annotation.RequiresPermission
import androidx.core.content.FileProvider
import com.crashlytics.android.Crashlytics
import com.elementary.tasks.BuildConfig
import timber.log.Timber
import java.io.File

object UriUtil {

    const val URI_MIME = "application/x-arc-uri-list"
    const val ANY_MIME = "any"
    const val IMAGE_MIME = "image/*"

    @RequiresPermission(Permissions.READ_EXTERNAL)
    fun obtainPath(cacheUtil: CacheUtil, uri: Uri, onReady: (Boolean, String?) -> Unit) {
        launchDefault {
            try {
                val path = cacheUtil.cacheFile(uri)
                if (path == null) {
                    withUIContext { onReady.invoke(false, null) }
                } else {
                    withUIContext { onReady.invoke(true, path) }
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
