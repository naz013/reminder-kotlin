package com.elementary.tasks.login

import android.content.Intent
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.graphics.drawable.Icon
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import com.elementary.tasks.R
import com.elementary.tasks.ReminderApp
import com.elementary.tasks.core.ThemedActivity
import com.elementary.tasks.core.appWidgets.UpdatesHelper
import com.elementary.tasks.core.cloud.Dropbox
import com.elementary.tasks.core.cloud.DropboxLogin
import com.elementary.tasks.core.cloud.GDrive
import com.elementary.tasks.core.cloud.GoogleLogin
import com.elementary.tasks.core.data.AppDb
import com.elementary.tasks.core.utils.*
import com.elementary.tasks.core.work.BackupDataWorker
import com.elementary.tasks.groups.GroupsUtil
import com.elementary.tasks.navigation.MainActivity
import com.elementary.tasks.notes.create.CreateNoteActivity
import com.elementary.tasks.reminder.create.CreateReminderActivity
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.view_progress.*
import java.io.IOException
import java.util.*
import javax.inject.Inject

/**
 * Copyright 2018 Nazar Suhovich
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
class LoginActivity : ThemedActivity() {

    private var googleLogin: GoogleLogin? = null
    private var dropboxLogin: DropboxLogin? = null

    @Inject
    lateinit var backupTool: BackupTool

    init {
        ReminderApp.appComponent.inject(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        if (Module.isPro) appNameBannerPro.visibility = View.VISIBLE
        else appNameBannerPro.visibility = View.GONE
        if (SuperUtil.isGooglePlayServicesAvailable(this)) {
            googleLogin = GoogleLogin(this, prefs)
        }
        dropboxLogin = DropboxLogin(this, object : DropboxLogin.LoginCallback {
            override fun onSuccess(b: Boolean) {
                if (b) loadDataFromDropbox()
            }
        })
        initButtons()
    }

    private fun showProgress(message: String?) {
        if (message == null) {
            setEnabling(true)
            progressView.visibility = View.INVISIBLE
        } else {
            setEnabling(false)
            progressMessageView.text = message
            progressView.visibility = View.VISIBLE
        }
    }

    override fun onResume() {
        super.onResume()
        dropboxLogin?.checkDropboxStatus()
    }

    private fun loadDataFromDropbox() {
        prefs.isBackupEnabled = true
        showProgress(getString(R.string.please_wait))
        launchDefault {
            val drive = Dropbox()

            withUIContext { showProgress(getString(R.string.syncing_groups)) }
            drive.downloadGroups(false)

            verifyGroups()

            withUIContext { showProgress(getString(R.string.syncing_reminders)) }
            drive.downloadReminders(false)

            //export & import notes
            withUIContext { showProgress(getString(R.string.syncing_notes)) }
            drive.downloadNotes(false)

            //export & import birthdays
            withUIContext { showProgress(getString(R.string.syncing_birthdays)) }
            drive.downloadBirthdays(false)

            //export & import places
            withUIContext { showProgress(getString(R.string.syncing_places)) }
            drive.downloadPlaces(false)

            //export & import templates
            withUIContext { showProgress(getString(R.string.syncing_templates)) }
            drive.downloadTemplates(false)
            drive.downloadSettings()

            withUIContext {
                finishRestoring()
            }
        }
    }

    private fun finishRestoring() {
        UpdatesHelper.updateWidget(this)
        UpdatesHelper.updateNotesWidget(this)
        showProgress(null)
        openApplication()
    }

    private fun showLoginError() {
        val builder = AlertDialog.Builder(this)
        builder.setMessage(getString(R.string.failed_to_login))
        builder.setPositiveButton(R.string.ok) { dialogInterface, _ -> dialogInterface.dismiss() }
        builder.create().show()
    }

    private fun initButtons() {
        if (SuperUtil.isGooglePlayServicesAvailable(this)) {
            google_button.visibility = View.VISIBLE
            google_button.setOnClickListener { googleLoginClick() }
        } else {
            google_button.visibility = View.GONE
        }
        local_button.setOnClickListener { restoreLocalData() }
        dropbox_button.setOnClickListener { loginToDropbox() }
        skip_button.setOnClickListener { openApplication() }
    }

    private fun initGroups() {
        if (AppDb.getAppDatabase(this).reminderGroupDao().all().isEmpty()) {
            GroupsUtil.initDefault(this)
        }
    }

    private fun loginToDropbox() {
        if (Permissions.ensurePermissions(this, PERM_DROPBOX, Permissions.READ_EXTERNAL, Permissions.WRITE_EXTERNAL)) {
            dropboxLogin?.login()
        }
    }

    private fun restoreLocalData() {
        if (!Permissions.ensurePermissions(this, PERM_LOCAL, Permissions.READ_EXTERNAL, Permissions.WRITE_EXTERNAL)) {
            return
        }
        prefs.isBackupEnabled = true
        showProgress(getString(R.string.please_wait))
        launchDefault {
            withUIContext { showProgress(getString(R.string.syncing_groups)) }
            try {
                backupTool.importGroups()
            } catch (ignored: IOException) {
            }

            verifyGroups()

            withUIContext { showProgress(getString(R.string.syncing_reminders)) }
            try {
                backupTool.importReminders()
            } catch (ignored: IOException) {
            }

            withUIContext { showProgress(getString(R.string.syncing_notes)) }
            try {
                backupTool.importNotes()
            } catch (ignored: IOException) {
            }

            withUIContext { showProgress(getString(R.string.syncing_birthdays)) }
            try {
                backupTool.importBirthdays()
            } catch (ignored: IOException) {
            }

            withUIContext { showProgress(getString(R.string.syncing_places)) }
            try {
                backupTool.importPlaces()
            } catch (ignored: IOException) {
            }

            withUIContext { showProgress(getString(R.string.syncing_templates)) }
            try {
                backupTool.importTemplates()
            } catch (ignored: IOException) {
            }

            prefs.loadPrefsFromFile()

            withUIContext {
                finishRestoring()
            }
        }
    }

    private fun verifyGroups() {
        val list = AppDb.getAppDatabase(this).reminderGroupDao().all()
        if (list.isEmpty()) {
            val defUiID = GroupsUtil.initDefault(this)
            val items = AppDb.getAppDatabase(this).reminderDao().all()
            val dao = AppDb.getAppDatabase(this).reminderDao()
            for (item in items) {
                item.groupUuId = defUiID
                dao.insert(item)
            }
            BackupDataWorker.schedule()
        }
    }

    private fun loadDataFromGoogle() {
        prefs.isBackupEnabled = true
        showProgress(getString(R.string.please_wait))
        launchDefault {
            val drive = GDrive.getInstance(this@LoginActivity)
            if (drive != null) {
                withUIContext { showProgress(getString(R.string.syncing_groups)) }
                try {
                    drive.downloadGroups(false)
                } catch (e: Exception) {
                }

                verifyGroups()

                withUIContext { showProgress(getString(R.string.syncing_reminders)) }
                try {
                    drive.downloadReminders(false)
                } catch (e: Exception) {
                }

                //export & import notes
                withUIContext { showProgress(getString(R.string.syncing_notes)) }
                try {
                    drive.downloadNotes(false)
                } catch (e: Exception) {
                }

                //export & import birthdays
                withUIContext { showProgress(getString(R.string.syncing_birthdays)) }
                try {
                    drive.downloadBirthdays(false)
                } catch (e: Exception) {
                }

                //export & import places
                withUIContext { showProgress(getString(R.string.syncing_places)) }
                try {
                    drive.downloadPlaces(false)
                } catch (e: Exception) {
                }

                //export & import templates
                withUIContext { showProgress(getString(R.string.syncing_templates)) }
                try {
                    drive.downloadTemplates(false)
                } catch (e: Exception) {
                }

                try {
                    drive.downloadSettings(false)
                } catch (e: Exception) {
                }
            }

            withUIContext {
                finishRestoring()
            }
        }
    }

    private fun openApplication() {
        enableShortcuts()
        launchDefault {
            initGroups()
            prefs.isUserLogged = true
            withUIContext {
                startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                finish()
            }
        }
    }

    private fun enableShortcuts() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            val shortcutManager = getSystemService(ShortcutManager::class.java)

            val shortcut = ShortcutInfo.Builder(this, "id.reminder")
                    .setShortLabel(getString(R.string.add_reminder_menu))
                    .setLongLabel(getString(R.string.add_reminder_menu))
                    .setIcon(Icon.createWithResource(this, R.drawable.add_reminder_shortcut))
                    .setIntents(arrayOf(Intent(Intent.ACTION_MAIN).setClass(this, MainActivity::class.java),
                            Intent(Intent.ACTION_VIEW).setClass(this, CreateReminderActivity::class.java)))
                    .build()

            val shortcut2 = ShortcutInfo.Builder(this, "id.note")
                    .setShortLabel(getString(R.string.add_note))
                    .setLongLabel(getString(R.string.add_note))
                    .setIcon(Icon.createWithResource(this, R.drawable.add_note_shortcut))
                    .setIntents(arrayOf(Intent(Intent.ACTION_MAIN).setClass(this, MainActivity::class.java),
                            Intent(Intent.ACTION_VIEW).setClass(this, CreateNoteActivity::class.java)))
                    .build()
            if (shortcutManager != null) {
                shortcutManager.dynamicShortcuts = Arrays.asList(shortcut, shortcut2)
            }
        }
    }

    private fun googleLoginClick() {
        if (Permissions.ensurePermissions(this, PERM, Permissions.GET_ACCOUNTS, Permissions.READ_EXTERNAL,
                        Permissions.WRITE_EXTERNAL)) {
            googleLogin?.loginDrive(object : GoogleLogin.DriveCallback {
                override fun onProgress(isLoading: Boolean) {

                }

                override fun onResult(v: GDrive?, isLogged: Boolean) {
                    if (isLogged) loadDataFromGoogle()
                    else showLoginError()
                }

                override fun onFail() {
                    showLoginError()
                }
            })
        }
    }

    private fun setEnabling(b: Boolean) {
        dropbox_button.isEnabled = b
        google_button.isEnabled = b
        local_button.isEnabled = b
        skip_button.isEnabled = b
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        googleLogin?.onActivityResult(requestCode, resultCode, data)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (Permissions.isAllGranted(grantResults)) {
            when (requestCode) {
                PERM -> googleLoginClick()
                PERM_DROPBOX -> loginToDropbox()
                PERM_LOCAL -> restoreLocalData()
            }
        }
    }

    companion object {

        private const val PERM = 103
        private const val PERM_DROPBOX = 104
        private const val PERM_LOCAL = 105
    }
}
