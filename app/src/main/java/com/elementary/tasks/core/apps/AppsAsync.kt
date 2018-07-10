package com.elementary.tasks.core.apps

import android.app.ProgressDialog
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.AsyncTask

import com.elementary.tasks.R

import java.util.ArrayList

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

class AppsAsync(private val mContext: Context, private val mListener: LoadListener?) : AsyncTask<Void, Void, Void>() {

    private var mDialog: ProgressDialog? = null
    private var mList: MutableList<ApplicationItem>? = null

    override fun onPreExecute() {
        super.onPreExecute()
        mDialog = ProgressDialog.show(mContext, null, mContext.getString(R.string.please_wait), true)
    }

    override fun doInBackground(vararg params: Void): Void? {
        mList = ArrayList()
        mList!!.clear()
        val pm = mContext.packageManager
        val packages = pm.getInstalledApplications(PackageManager.GET_META_DATA)
        for (packageInfo in packages) {
            val name = packageInfo.loadLabel(pm).toString()
            val packageName = packageInfo.packageName
            val drawable = packageInfo.loadIcon(pm)
            val data = ApplicationItem(name, packageName, drawable)
            val pos = getPosition(name)
            if (pos == -1) {
                mList!!.add(data)
            } else {
                mList!!.add(getPosition(name), data)
            }
        }
        return null
    }

    private fun getPosition(name: String): Int {
        if (mList!!.size == 0) {
            return 0
        }
        var position = -1
        for (data in mList!!) {
            val comp = name.compareTo(data.name!!)
            if (comp <= 0) {
                position = mList!!.indexOf(data)
                break
            }
        }
        return position
    }

    override fun onPostExecute(aVoid: Void) {
        super.onPostExecute(aVoid)
        if (mDialog != null && mDialog!!.isShowing) {
            try {
                mDialog!!.dismiss()
            } catch (ignored: IllegalArgumentException) {
            }

        }
        mListener?.onLoaded(mList)
    }
}
