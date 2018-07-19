package com.elementary.tasks.navigation.settings.images

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.AsyncTask
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.FileProvider

import com.bumptech.glide.Glide
import com.elementary.tasks.BuildConfig
import com.elementary.tasks.R
import com.elementary.tasks.core.network.RetrofitBuilder
import com.elementary.tasks.core.utils.Module
import com.elementary.tasks.core.utils.Notifier

import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.ExecutionException

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

internal class DownloadAsync(private val mContext: Context, private val fileName: String,
                             private val filePath: String, private val width: Int,
                             private val height: Int, private val id: Long) : AsyncTask<String, Void, DownloadAsync.Image>() {
    private var mNotifyMgr: NotificationManagerCompat? = null
    private val builder: NotificationCompat.Builder = NotificationCompat.Builder(mContext, Notifier.CHANNEL_SYSTEM)

    override fun onPreExecute() {
        super.onPreExecute()
        builder.setContentTitle(fileName)
        builder.setContentText(mContext.getString(R.string.downloading_start))
        if (Module.isLollipop) {
            builder.setSmallIcon(R.drawable.ic_get_app_white_24dp)
        } else {
            builder.setSmallIcon(R.drawable.ic_get_up_nv_white)
        }
        mNotifyMgr = NotificationManagerCompat.from(mContext)
        mNotifyMgr!!.notify(id.toInt(), builder.build())
    }

    override fun doInBackground(vararg strings: String): DownloadAsync.Image? {
        var image: Image? = null
        val file = File(filePath)
        try {
            val bitmap = Glide.with(mContext).asBitmap().load(RetrofitBuilder.getImageLink(id, width, height)).submit().get()
            try {
                if (file.exists()) {
                    file.delete()
                }
                if (file.createNewFile()) {
                    val stream = FileOutputStream(file)
                    bitmap!!.compress(Bitmap.CompressFormat.JPEG, 100, stream)
                    stream.close()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            if (bitmap != null) {
                image = Image()
                image.path = filePath
            }
        } catch (e: InterruptedException) {
            e.printStackTrace()
        } catch (e: ExecutionException) {
            e.printStackTrace()
        }
        return image
    }

    override fun onPostExecute(aVoid: DownloadAsync.Image?) {
        super.onPostExecute(aVoid)
        if (aVoid != null) {
            showNotificationWithImage(aVoid)
        } else {
            showErrorNotification()
        }
    }

    private fun showErrorNotification() {
        builder.setContentText(mContext.getString(R.string.download_failed))
        if (Module.isLollipop) {
            builder.setSmallIcon(R.drawable.ic_warning_white_24dp)
        } else {
            builder.setSmallIcon(R.drawable.ic_warning_nv_white)
        }
        builder.setAutoCancel(true)
        builder.setWhen(System.currentTimeMillis())
        mNotifyMgr!!.notify(id.toInt(), builder.build())
    }

    private fun showNotificationWithImage(image: Image) {
        val intent = Intent()
        intent.action = Intent.ACTION_VIEW
        if (Module.isNougat) {
            val uri = FileProvider.getUriForFile(mContext, BuildConfig.APPLICATION_ID + ".provider", File(image.path!!))
            intent.data = uri
            intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        } else {
            intent.setDataAndType(Uri.parse("file://" + image.path!!), "image/*")
        }
        val pendingIntent = PendingIntent.getActivity(mContext, 0, intent, 0)
        builder.setContentIntent(pendingIntent)
        builder.setContentText(mContext.getString(R.string.click_to_preview))
        if (Module.isLollipop) {
            builder.setSmallIcon(R.drawable.ic_done_white_24dp)
        } else {
            builder.setSmallIcon(R.drawable.ic_done_nv_white)
        }
        builder.setAutoCancel(true)
        builder.setWhen(System.currentTimeMillis())
        mNotifyMgr!!.notify(id.toInt(), builder.build())
    }

    inner class Image {

        var path: String? = null
    }
}
