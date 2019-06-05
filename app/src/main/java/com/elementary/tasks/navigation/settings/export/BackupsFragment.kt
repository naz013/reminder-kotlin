package com.elementary.tasks.navigation.settings.export

import android.os.Bundle
import android.os.Environment
import android.os.StatFs
import android.view.View
import android.widget.Toast
import com.elementary.tasks.R
import com.elementary.tasks.core.cloud.Dropbox
import com.elementary.tasks.core.cloud.GDrive
import com.elementary.tasks.core.utils.*
import com.elementary.tasks.databinding.FragmentSettingsBackupsBinding
import com.elementary.tasks.navigation.settings.BaseSettingsFragment
import com.elementary.tasks.navigation.settings.export.backups.InfoAdapter
import com.elementary.tasks.navigation.settings.export.backups.UserItem
import kotlinx.coroutines.Job
import java.io.File
import java.io.IOException
import java.util.*

class BackupsFragment : BaseSettingsFragment<FragmentSettingsBackupsBinding>() {

    private var mAdapter: InfoAdapter? = null
    private var mJob: Job? = null

    private val localFolders: List<File?>
        get() {
            val r = MemoryUtil.remindersDir
            val n = MemoryUtil.notesDir
            val g = MemoryUtil.groupsDir
            val b = MemoryUtil.birthdaysDir
            val p = MemoryUtil.placesDir
            val s = MemoryUtil.prefsDir
            val t = MemoryUtil.templatesDir
            return listOf(r, n, g, b, p, s, t)
        }

    private val googleFolders: List<File?>
        get() {
            val r = MemoryUtil.googleRemindersDir
            val n = MemoryUtil.googleNotesDir
            val g = MemoryUtil.googleGroupsDir
            val b = MemoryUtil.googleBirthdaysDir
            val p = MemoryUtil.googlePlacesDir
            val s = MemoryUtil.googlePrefsDir
            val t = MemoryUtil.googleTemplatesDir
            return listOf(r, n, g, b, p, s, t)
        }

    private val dropboxFolders: List<File?>
        get() {
            val r = MemoryUtil.dropboxRemindersDir
            val n = MemoryUtil.dropboxNotesDir
            val g = MemoryUtil.dropboxGroupsDir
            val b = MemoryUtil.dropboxBirthdaysDir
            val p = MemoryUtil.dropboxPlacesDir
            val s = MemoryUtil.dropboxPrefsDir
            val t = MemoryUtil.dropboxTemplatesDir
            return listOf(r, n, g, b, p, s, t)
        }

    private fun cancelTask() {
        mJob?.cancel()
    }

    override fun layoutRes(): Int = R.layout.fragment_settings_backups

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initProgress()
        ViewUtils.listenScrollableView(binding.scrollView) {
            setToolbarAlpha(toAlpha(it.toFloat(), NESTED_SCROLL_MAX))
        }

        mAdapter = InfoAdapter(binding.itemsContainer) {
            if (it != null) {
                deleteFiles(getFolders(it), it)
            }
        }

        loadUserInfo()
    }

    private fun initProgress() {
        binding.progressMessageView.setText(R.string.please_wait)
        hideProgress()
    }

    override fun getTitle(): String = getString(R.string.backup_files)

    private fun getFolders(info: Info): List<File?> {
        return when (info) {
            Info.Dropbox -> dropboxFolders
            Info.Google -> googleFolders
            else -> localFolders
        }
    }

    private fun loadUserInfo() {
        withActivity {
            if (!Permissions.checkPermission(it, SD_CODE, Permissions.READ_EXTERNAL)) {
                return@withActivity
            }
            val list = ArrayList<Info>()
            list.add(Info.Local)
            val dbx = Dropbox()
            dbx.startSession()
            if (dbx.isLinked) {
                list.add(Info.Dropbox)
            }
            val gdx = GDrive.getInstance(it)
            if (gdx != null) {
                list.add(Info.Google)
            }
            loadInfo(list)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            SD_CODE -> if (Permissions.checkPermission(grantResults)) {
                loadUserInfo()
            }
        }
    }

    private fun loadInfo(infos: List<Info>) {
        mJob?.cancel()
        showProgress()
        mJob = launchDefault {
            val list = ArrayList<UserItem>()
            for (i in infos.indices) {
                when (infos[i]) {
                    Info.Dropbox -> addDropboxData(list)
                    Info.Google -> addGoogleData(list)
                    Info.Local -> addLocalData(list)
                }
            }
            withUIContext {
                mJob = null
                hideProgress()
                mAdapter?.setData(list)
            }
        }
    }

    private fun showProgress() {
        binding.progressView.visibility = View.VISIBLE
    }

    private fun hideProgress() {
        binding.progressView.visibility = View.GONE
    }

    private fun deleteFiles(params: List<File?>, type: Info) {
        mJob = null
        val context = context ?: return

        showProgress()
        launchDefault {
            if (type == Info.Dropbox) {
                val dbx = Dropbox()
                dbx.startSession()
                val isLinked = dbx.isLinked
                for (file in params) {
                    if (file == null || !file.exists()) {
                        continue
                    }
                    if (file.isDirectory) {
                        val files = file.listFiles() ?: continue
                        for (f in files) {
                            f.delete()
                        }
                    } else {
                        if (file.delete()) {
                        }
                    }
                }
                if (isLinked) {
                    dbx.cleanFolder()
                }
            } else if (type == Info.Google) {
                val gdx = GDrive.getInstance(context)
                val isLinked = gdx != null
                for (file in params) {
                    if (file == null || !file.exists()) {
                        continue
                    }
                    if (file.isDirectory) {
                        val files = file.listFiles() ?: continue
                        for (f in files) {
                            f.delete()
                        }
                    } else {
                        if (file.delete()) {
                        }
                    }
                }
                if (isLinked && gdx != null) {
                    try {
                        gdx.cleanFolder()
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }

                }
            } else if (type == Info.Local) {
                for (file in params) {
                    if (file == null || !file.exists()) {
                        continue
                    }
                    if (file.isDirectory) {
                        val files = file.listFiles() ?: continue
                        for (f in files) {
                            f.delete()
                        }
                    } else {
                        if (file.delete()) {
                        }
                    }
                }
            }
            withUIContext {
                hideProgress()
                Toast.makeText(context, R.string.all_files_removed, Toast.LENGTH_SHORT).show()
                loadUserInfo()
            }
        }
    }

    private fun addLocalData(list: MutableList<UserItem>) {
        val path = Environment.getExternalStorageDirectory()
        val stat = StatFs(path.path)
        val blockSize = stat.blockSizeLong
        val totalBlocks = stat.blockCountLong
        val availableBlocks = stat.availableBlocksLong
        val totalSize = blockSize * totalBlocks
        val userItem = UserItem()
        userItem.quota = totalSize
        userItem.used = totalSize - availableBlocks * blockSize
        userItem.kind = Info.Local
        getCountFiles(userItem)
        list.add(userItem)
    }

    private fun addDropboxData(list: MutableList<UserItem>) {
        val dbx = Dropbox()
        dbx.startSession()
        if (dbx.isLinked) {
            val quota = dbx.userQuota()
            val quotaUsed = dbx.userQuotaNormal()
            val name = dbx.userName()
            val count = dbx.countFiles()
            val userItem = UserItem(name = name, quota = quota, used = quotaUsed, count = count, photo = "")
            userItem.kind = Info.Dropbox
            list.add(userItem)
        }
    }

    private fun addGoogleData(list: MutableList<UserItem>) {
        withContext {
            val gdx = GDrive.getInstance(it)
            if (gdx != null) {
                val userItem = gdx.data
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

    override fun onDestroy() {
        super.onDestroy()
        cancelTask()
    }

    enum class Info {
        Dropbox, Google, Local
    }

    companion object {
        private const val SD_CODE = 623

        fun newInstance(): BackupsFragment {
            return BackupsFragment()
        }
    }
}
