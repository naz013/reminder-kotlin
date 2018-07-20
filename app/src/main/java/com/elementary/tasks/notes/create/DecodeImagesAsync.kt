package com.elementary.tasks.notes.create

import android.app.ProgressDialog
import android.content.ClipData
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.AsyncTask
import com.elementary.tasks.R
import com.elementary.tasks.core.utils.BitmapUtils
import java.io.ByteArrayOutputStream
import java.io.FileNotFoundException
import java.util.*

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
class DecodeImagesAsync internal constructor(private val mContext: Context,
                                             private val mCallback: ((List<NoteImage>) -> Unit)?,
                                             private val max: Int) : AsyncTask<ClipData, Int, List<NoteImage>>() {
    private var mDialog: ProgressDialog? = null

    override fun onPreExecute() {
        super.onPreExecute()
        val mDialog = ProgressDialog(mContext)
        mDialog.setTitle(R.string.please_wait)
        mDialog.setMessage(mContext.getString(R.string.decoding_images))
        mDialog.setCancelable(false)
        if (max > 1) {
            mDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL)
            mDialog.max = max
            mDialog.isIndeterminate = false
            mDialog.progress = 1
        } else {
            mDialog.isIndeterminate = false
        }
        this.mDialog = mDialog
        mDialog.show()
    }

    override fun onProgressUpdate(vararg values: Int?) {
        super.onProgressUpdate(*values)
        if (mDialog != null && mDialog!!.isShowing) {
            mDialog!!.progress = values[0]!!
        }
    }

    override fun doInBackground(vararg clipDatas: ClipData): List<NoteImage> {
        val list = ArrayList<NoteImage>()
        val mClipData = clipDatas[0]
        for (i in 0 until mClipData.itemCount) {
            publishProgress(i + 1)
            val item = mClipData.getItemAt(i)
            addImageFromUri(list, item.uri)
        }
        return list
    }

    private fun addImageFromUri(images: MutableList<NoteImage>, uri: Uri?) {
        if (uri == null) {
            return
        }
        var bitmapImage: Bitmap? = null
        try {
            bitmapImage = BitmapUtils.decodeUriToBitmap(mContext, uri)
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        }

        if (bitmapImage != null) {
            val outputStream = ByteArrayOutputStream()
            bitmapImage.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
            images.add(NoteImage(outputStream.toByteArray()))
        }
    }

    override fun onPostExecute(noteImages: List<NoteImage>) {
        super.onPostExecute(noteImages)
        try {
            if (mDialog != null && mDialog!!.isShowing) {
                mDialog!!.dismiss()
            }
        } catch (ignored: IllegalArgumentException) {
        }
        mCallback?.invoke(noteImages)
    }
}
