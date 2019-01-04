package com.elementary.tasks.login

import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.graphics.drawable.Icon
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.text.SpannableStringBuilder
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.URLSpan
import android.view.View
import android.webkit.WebView
import android.widget.CheckBox
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
import com.elementary.tasks.groups.GroupsUtil
import com.elementary.tasks.navigation.MainActivity
import com.elementary.tasks.notes.create.CreateNoteActivity
import com.elementary.tasks.reminder.createEdit.CreateReminderActivity
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
    lateinit var updatesHelper: UpdatesHelper
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
        googleLogin = GoogleLogin(this, prefs)
        dropboxLogin = DropboxLogin(this, object : DropboxLogin.LoginCallback {
            override fun onSuccess(b: Boolean) {
                if (b) loadDataFromDropbox()
            }
        })
        initButtons()
        initCheckbox()
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
        updatesHelper.updateWidget()
        updatesHelper.updateNotesWidget()
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
        google_button.setOnClickListener { googleLoginClick() }
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
        if (Permissions.checkPermission(this, Permissions.READ_EXTERNAL, Permissions.WRITE_EXTERNAL)) {
            dropboxLogin?.login()
        } else {
            Permissions.requestPermission(this, PERM_DROPBOX, Permissions.READ_EXTERNAL, Permissions.WRITE_EXTERNAL)
        }
    }

    private fun restoreLocalData() {
        if (!Permissions.checkPermission(this, Permissions.READ_EXTERNAL, Permissions.WRITE_EXTERNAL)) {
            Permissions.requestPermission(this, PERM_LOCAL, Permissions.READ_EXTERNAL, Permissions.WRITE_EXTERNAL)
            return
        }
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
                backupTool.importReminders(this@LoginActivity)
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
        }
    }

    private fun loadDataFromGoogle() {
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
        if (Permissions.checkPermission(this, Permissions.GET_ACCOUNTS, Permissions.READ_EXTERNAL,
                        Permissions.WRITE_EXTERNAL)) {
            googleLogin?.loginDrive(object : GoogleLogin.DriveCallback {
                override fun onProgress(isLoading: Boolean) {

                }

                override fun onResult(v: GDrive?, isLogged: Boolean) {
                    if (isLogged) loadDataFromGoogle()
                }

                override fun onFail() {
                    showLoginError()
                }
            })
        } else {
            Permissions.requestPermission(this, PERM, Permissions.GET_ACCOUNTS,
                    Permissions.READ_EXTERNAL, Permissions.WRITE_EXTERNAL)
        }
    }

    private fun initCheckbox() {
        setViewHTML(terms_check_box, getString(R.string.i_accept))
        terms_check_box.setOnCheckedChangeListener { _, b -> setEnabling(b) }
        terms_check_box.isChecked = true
    }

    private fun setEnabling(b: Boolean) {
        dropbox_button.isEnabled = b
        google_button.isEnabled = b
        local_button.isEnabled = b
        skip_button.isEnabled = b
    }

    private fun makeLinkClickable(strBuilder: SpannableStringBuilder, span: URLSpan) {
        val start = strBuilder.getSpanStart(span)
        val end = strBuilder.getSpanEnd(span)
        val flags = strBuilder.getSpanFlags(span)
        strBuilder.setSpan(object : ClickableSpan() {
            override fun onClick(view: View) {
                if (span.url.contains(TERMS_URL)) {
                    openTermsScreen()
                }
            }
        }, start, end, flags)
        strBuilder.removeSpan(span)
    }

    private fun openTermsScreen() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(getString(R.string.privacy_policy))
        val webView = WebView(this)
        webView.loadUrl("https://craysoftware.wordpress.com/privacy-policy/")
        builder.setView(webView)
        builder.setPositiveButton(R.string.ok) { dialogInterface, _ -> dialogInterface.dismiss() }
        builder.create().show()
    }

    private fun setViewHTML(text: CheckBox, html: String) {
        val sequence = Html.fromHtml(html)
        val strBuilder = SpannableStringBuilder(sequence)
        val urls = strBuilder.getSpans(0, sequence.length, URLSpan::class.java)
        for (span in urls) {
            makeLinkClickable(strBuilder, span)
        }
        text.text = strBuilder
        text.movementMethod = LinkMovementMethod.getInstance()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        googleLogin?.onActivityResult(requestCode, resultCode, data)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            PERM -> if (grantResults[0] == PackageManager.PERMISSION_GRANTED) googleLoginClick()
            PERM_DROPBOX -> if (grantResults[0] == PackageManager.PERMISSION_GRANTED) loginToDropbox()
            PERM_LOCAL -> if (grantResults[0] == PackageManager.PERMISSION_GRANTED) restoreLocalData()
        }
    }

    companion object {

        private const val PERM = 103
        private const val PERM_DROPBOX = 104
        private const val PERM_LOCAL = 105
        private const val TERMS_URL = "termsopen.com"
    }
}
