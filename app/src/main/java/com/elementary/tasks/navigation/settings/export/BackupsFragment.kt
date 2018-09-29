package com.elementary.tasks.navigation.settings.export

import android.content.pm.PackageManager
import android.os.Bundle
import android.view.*
import android.widget.Toast
import com.elementary.tasks.R
import com.elementary.tasks.backups.DeleteAsync
import com.elementary.tasks.backups.InfoAdapter
import com.elementary.tasks.backups.UserInfoAsync
import com.elementary.tasks.core.cloud.Dropbox
import com.elementary.tasks.core.cloud.Google
import com.elementary.tasks.core.utils.MemoryUtil
import com.elementary.tasks.core.utils.Permissions
import com.elementary.tasks.navigation.fragments.BaseNavigationFragment
import kotlinx.android.synthetic.main.fragment_backups.*
import java.io.File
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

class BackupsFragment : BaseNavigationFragment() {

    private var mAdapter: InfoAdapter? = null
    private var mTask: UserInfoAsync? = null

    private val localFolders: Array<File?>
        get() {
            val r = MemoryUtil.remindersDir
            val n = MemoryUtil.notesDir
            val g = MemoryUtil.groupsDir
            val b = MemoryUtil.birthdaysDir
            val p = MemoryUtil.placesDir
            val s = MemoryUtil.prefsDir
            val t = MemoryUtil.templatesDir
            return arrayOf(r, n, g, b, p, s, t)
        }

    private val googleFolders: Array<File?>
        get() {
            val r = MemoryUtil.googleRemindersDir
            val n = MemoryUtil.googleNotesDir
            val g = MemoryUtil.googleGroupsDir
            val b = MemoryUtil.googleBirthdaysDir
            val p = MemoryUtil.googlePlacesDir
            val s = MemoryUtil.googlePrefsDir
            val t = MemoryUtil.googleTemplatesDir
            return arrayOf(r, n, g, b, p, s, t)
        }

    private val dropboxFolders: Array<File?>
        get() {
            val r = MemoryUtil.dropboxRemindersDir
            val n = MemoryUtil.dropboxNotesDir
            val g = MemoryUtil.dropboxGroupsDir
            val b = MemoryUtil.dropboxBirthdaysDir
            val p = MemoryUtil.dropboxPlacesDir
            val s = MemoryUtil.dropboxPrefsDir
            val t = MemoryUtil.dropboxTemplatesDir
            return arrayOf(r, n, g, b, p, s, t)
        }

    private fun cancelTask() {
        if (mTask != null) {
            mTask!!.cancel(true)
        }
    }

    private fun deleteFiles(info: UserInfoAsync.Info) {
        DeleteAsync(context!!, { this.loadUserInfo() }, info).execute(*getFolders(info))
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater!!.inflate(R.menu.backup_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item!!.itemId) {
            R.id.action_scan -> {
                loadUserInfo()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_backups, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mAdapter = InfoAdapter(itemsContainer) {
            if (it != null) this.deleteFiles(it)
        }
    }

    override fun onResume() {
        super.onResume()
        loadUserInfo()
        if (callback != null) {
            callback!!.onTitleChange(getString(R.string.backup_files))
            callback!!.onFragmentSelect(this)
        }
    }

    private fun getFolders(info: UserInfoAsync.Info): Array<File?> {
        return when (info) {
            UserInfoAsync.Info.Dropbox -> dropboxFolders
            UserInfoAsync.Info.Google -> googleFolders
            else -> localFolders
        }
    }

    private fun loadUserInfo() {
        if (!Permissions.checkPermission(activity!!, Permissions.READ_EXTERNAL)) {
            Permissions.requestPermission(activity!!, SD_CODE, Permissions.READ_EXTERNAL, Permissions.WRITE_EXTERNAL)
            return
        }
        val list = ArrayList<UserInfoAsync.Info>()
        list.add(UserInfoAsync.Info.Local)
        val dbx = Dropbox()
        dbx.startSession()
        if (dbx.isLinked) {
            list.add(UserInfoAsync.Info.Dropbox)
        }
        val gdx = Google.getInstance()
        if (gdx != null) {
            list.add(UserInfoAsync.Info.Google)
        }
        val array = arrayOfNulls<UserInfoAsync.Info>(list.size)
        for (i in list.indices) {
            array[i] = list[i]
        }
        cancelTask()
        mTask = UserInfoAsync(context!!, {
            mAdapter?.setData(it)
        }, list.size, {
            Toast.makeText(context, R.string.canceled, Toast.LENGTH_SHORT).show()
            cancelTask()
        })
        mTask!!.execute(*array)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults.isEmpty()) return
        when (requestCode) {
            SD_CODE -> if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                loadUserInfo()
            }
        }
    }

    companion object {
        private const val SD_CODE = 623
    }
}
