package com.elementary.tasks.core.calendar

import android.content.Context
import android.graphics.Bitmap
import android.net.ConnectivityManager
import android.os.AsyncTask
import android.os.Environment
import com.bumptech.glide.Glide
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
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
class LoadAsync(private val context: Context, private val month: Int, private val id: Long) : AsyncTask<Void, Void, Void>() {

    override fun doInBackground(vararg params: Void): Void? {
        val connManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI)
        if (!ImageCheck.getInstance().isImage(month, id) && mWifi.isConnected) {
            try {
                val bitmap = Glide.with(context)
                        .asBitmap()
                        .load(ImageCheck.getInstance().getImageUrl(month, id))
                        .submit()
                        .get()
                val sdPath = Environment.getExternalStorageDirectory()
                val sdPathDr = File(sdPath.toString() + "/JustReminder/" + "image_cache")
                if (!sdPathDr.exists()) {
                    sdPathDr.mkdirs()
                }
                val image = File(sdPathDr, ImageCheck.getInstance().getImageName(month, id))
                if (image.createNewFile()) {
                    val stream = FileOutputStream(image)
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
                    stream.close()
                }
            } catch (e: IOException) {
                e.printStackTrace()
            } catch (e: InterruptedException) {
                e.printStackTrace()
            } catch (e: ExecutionException) {
                e.printStackTrace()
            }

        }
        return null
    }
}
