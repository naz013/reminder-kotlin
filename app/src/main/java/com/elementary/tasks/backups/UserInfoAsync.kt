package com.elementary.tasks.backups

import android.app.ProgressDialog
import android.content.Context
import android.os.AsyncTask
import android.os.Environment
import android.os.StatFs
import com.elementary.tasks.R
import com.elementary.tasks.core.cloud.Dropbox
import com.elementary.tasks.core.cloud.Google
import com.elementary.tasks.core.utils.MemoryUtil
import com.elementary.tasks.core.utils.Module
import com.elementary.tasks.core.utils.SuperUtil
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

class UserInfoAsync(private val mContext: Context, private val listener: ((List<UserItem>) -> Unit)?,
                    private val count: Int, private val mDialogListener: (() -> Unit)?) : AsyncTask<UserInfoAsync.Info, Int, List<UserItem>>() {

    private var mDialog: ProgressDialog? = null

    enum class Info {
        Dropbox, Google, Local
    }

    override fun onPreExecute() {
        super.onPreExecute()
        val dialog = ProgressDialog(mContext)
        dialog.setMessage(mContext.getString(R.string.retrieving_data))
        dialog.setCancelable(true)
        dialog.setOnCancelListener {
            mDialogListener?.invoke()
        }
        if (count > 1) {
            dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL)
            dialog.max = count
            dialog.isIndeterminate = false
            dialog.progress = 1
        } else {
            dialog.isIndeterminate = false
        }
        dialog.show()
        mDialog = dialog
    }

    override fun onProgressUpdate(vararg values: Int?) {
        super.onProgressUpdate(*values)
        if (mDialog != null && mDialog!!.isShowing) {
            mDialog!!.progress = values[0]!!
        }
    }

    override fun doInBackground(vararg infos: Info): List<UserItem> {
        val list = ArrayList<UserItem>()
        for (i in infos.indices) {
            val info = infos[i]
            when (info) {
                Info.Dropbox -> addDropboxData(list)
                Info.Google -> addGoogleData(list)
                Info.Local -> addLocalData(list)
            }
            publishProgress(i + 1)
        }
        return list
    }

    override fun onPostExecute(list: List<UserItem>) {
        super.onPostExecute(list)
        if (mDialog != null && mDialog!!.isShowing) {
            try {
                mDialog!!.dismiss()
            } catch (ignored: IllegalArgumentException) {
            }

        }
        listener?.invoke(list)
    }

    private fun addLocalData(list: MutableList<UserItem>) {
        val path = Environment.getExternalStorageDirectory()
        val stat = StatFs(path.path)
        val blockSize: Long
        val totalBlocks: Long
        val availableBlocks: Long
        if (Module.isJellyMR2) {
            blockSize = stat.blockSizeLong
            totalBlocks = stat.blockCountLong
            availableBlocks = stat.availableBlocksLong
        } else {
            blockSize = stat.blockSize.toLong()
            totalBlocks = stat.blockCount.toLong()
            availableBlocks = stat.blockCount.toLong()
        }
        val totalSize = blockSize * totalBlocks
        val userItem = UserItem()
        userItem.quota = totalSize
        userItem.used = totalSize - availableBlocks * blockSize
        userItem.kind = Info.Local
        getCountFiles(userItem)
        list.add(userItem)
    }

    private fun addDropboxData(list: MutableList<UserItem>) {
        val dbx = Dropbox(mContext)
        dbx.startSession()
        if (dbx.isLinked && SuperUtil.isConnected(mContext)) {
            val quota = dbx.userQuota()
            val quotaUsed = dbx.userQuotaNormal()
            val name = dbx.userName()
            val count = dbx.countFiles()
            val userItem = UserItem(name, quota, quotaUsed, count, "")
            userItem.kind = Info.Dropbox
            list.add(userItem)
        }
    }

    private fun addGoogleData(list: MutableList<UserItem>) {
        val gdx = Google.getInstance()
        if (gdx != null && SuperUtil.isConnected(mContext)) {
            val drives = gdx.drive
            if (drives != null) {
                val userItem = drives.data
                if (userItem != null) {
                    userItem.kind = Info.Google
                    list.add(userItem)
                }
            }
        }
    }

    private fun getCountFiles(item: UserItem) {
        var count = 0
        var dir = MemoryUtil.remindersDir
        if (dir != null && dir.exists()) {
            val files = dir.listFiles()
            if (files != null) {
                count += files.size
            }
        }
        dir = MemoryUtil.notesDir
        if (dir != null && dir.exists()) {
            val files = dir.listFiles()
            if (files != null) {
                count += files.size
            }
        }
        dir = MemoryUtil.birthdaysDir
        if (dir != null && dir.exists()) {
            val files = dir.listFiles()
            if (files != null) {
                count += files.size
            }
        }
        dir = MemoryUtil.groupsDir
        if (dir != null && dir.exists()) {
            val files = dir.listFiles()
            if (files != null) {
                count += files.size
            }
        }
        dir = MemoryUtil.placesDir
        if (dir != null && dir.exists()) {
            val files = dir.listFiles()
            if (files != null) {
                count += files.size
            }
        }
        dir = MemoryUtil.templatesDir
        if (dir != null && dir.exists()) {
            val files = dir.listFiles()
            if (files != null) {
                count += files.size
            }
        }
        item.count = count
    }
}
