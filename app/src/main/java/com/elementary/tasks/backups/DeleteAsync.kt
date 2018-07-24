package com.elementary.tasks.backups

import android.app.ProgressDialog
import android.content.Context
import android.os.AsyncTask
import android.widget.Toast

import com.elementary.tasks.R
import com.elementary.tasks.core.cloud.Dropbox
import com.elementary.tasks.core.cloud.Google
import com.elementary.tasks.core.utils.SuperUtil

import java.io.File
import java.io.IOException

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

class DeleteAsync(private val mContext: Context, private val listener: (() -> Unit)?,
                  private val type: UserInfoAsync.Info) : AsyncTask<File, Void, Int>() {
    private var progressDialog: ProgressDialog? = null

    override fun onPreExecute() {
        super.onPreExecute()
        progressDialog = ProgressDialog.show(mContext, null, mContext.getString(R.string.deleting), false)
    }

    override fun doInBackground(vararg params: File): Int? {
        var res = 0
        if (type == UserInfoAsync.Info.Dropbox) {
            val dbx = Dropbox()
            dbx.startSession()
            val isLinked = dbx.isLinked
            val isConnected = SuperUtil.isConnected(mContext)
            for (file in params) {
                if (!file.exists()) {
                    continue
                }
                if (file.isDirectory) {
                    val files = file.listFiles() ?: continue
                    for (f in files) {
                        f.delete()
                    }
                    res = 2
                } else {
                    if (file.delete()) {
                        res = 1
                    }
                }
            }
            if (isLinked && isConnected) {
                dbx.cleanFolder()
            }
        } else if (type == UserInfoAsync.Info.Google) {
            val gdx = Google.getInstance()
            val isLinked = gdx != null
            val isConnected = SuperUtil.isConnected(mContext)
            for (file in params) {
                if (!file.exists()) {
                    continue
                }
                if (file.isDirectory) {
                    val files = file.listFiles() ?: continue
                    for (f in files) {
                        f.delete()
                    }
                    res = 2
                } else {
                    if (file.delete()) {
                        res = 1
                    }
                }
            }
            if (isLinked && isConnected && gdx!!.drive != null) {
                try {
                    gdx.drive!!.cleanFolder()
                } catch (e: IOException) {
                    e.printStackTrace()
                }

            }
        } else if (type == UserInfoAsync.Info.Local) {
            for (file in params) {
                if (!file.exists()) {
                    continue
                }
                if (file.isDirectory) {
                    val files = file.listFiles() ?: continue
                    for (f in files) {
                        f.delete()
                    }
                    res = 2
                } else {
                    if (file.delete()) {
                        res = 1
                    }
                }
            }
        }
        return res
    }

    override fun onPostExecute(aVoid: Int?) {
        super.onPostExecute(aVoid)
        if (progressDialog != null && progressDialog!!.isShowing) {
            progressDialog!!.dismiss()
        }
        Toast.makeText(mContext, R.string.all_files_removed, Toast.LENGTH_SHORT).show()
        listener?.invoke()
    }
}
