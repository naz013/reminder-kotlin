package com.elementary.tasks.login

import android.content.Intent
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.graphics.drawable.Drawable
import android.graphics.drawable.Icon
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.URLSpan
import android.view.View
import android.webkit.WebView
import android.widget.CheckBox

import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import com.elementary.tasks.R
import com.elementary.tasks.birthdays.work.CheckBirthdaysAsync
import com.elementary.tasks.core.ThemedActivity
import com.elementary.tasks.core.cloud.DropboxLogin
import com.elementary.tasks.core.cloud.GoogleLogin
import com.elementary.tasks.core.data.AppDb
import com.elementary.tasks.core.utils.Permissions
import com.elementary.tasks.core.utils.Prefs
import com.elementary.tasks.databinding.ActivityLoginBinding
import com.elementary.tasks.google_tasks.GetTaskListAsync
import com.elementary.tasks.google_tasks.TasksCallback
import com.elementary.tasks.groups.GroupsUtil
import com.elementary.tasks.navigation.MainActivity
import com.elementary.tasks.notes.create.CreateNoteActivity
import com.elementary.tasks.reminder.create_edit.CreateReminderActivity

import java.util.Arrays
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil

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

    private var binding: ActivityLoginBinding? = null

    private var googleLogin: GoogleLogin? = null
    private var dropboxLogin: DropboxLogin? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_login)
        googleLogin = GoogleLogin(this, object : GoogleLogin.LoginCallback {

            override fun onSuccess() {
                loadDataFromGoogle()
            }

            override fun onFail() {
                showLoginError()
            }
        })
        dropboxLogin = DropboxLogin(this) { logged -> if (logged) loadDataFromDropbox() }
        initButtons()
        loadPhotoView()
        initCheckbox()
    }

    private fun loadPhotoView() {
        val myOptions = RequestOptions()
                .centerCrop()
                .override(768, 1280)

        Glide.with(this)
                .load("https://unsplash.it/1080/1920?image=596&blur")
                .apply(myOptions)
                .into<>(object : SimpleTarget<Drawable>() {
                    override fun onResourceReady(resource: Drawable, transition: Transition<in Drawable>?) {
                        binding!!.imageView2.setImageDrawable(resource)
                    }

                    override fun onLoadFailed(errorDrawable: Drawable?) {
                        super.onLoadFailed(errorDrawable)
                        loadDefaultImage()
                    }
                })
    }

    private fun loadDefaultImage() {
        val myOptions = RequestOptions()
                .centerCrop()
                .override(768, 1280)

        Glide.with(this)
                .load(R.drawable.photo)
                .apply(myOptions)
                .into(binding!!.imageView2)
    }

    override fun onResume() {
        super.onResume()
        dropboxLogin!!.checkDropboxStatus()
    }

    private fun loadDataFromDropbox() {
        RestoreDropboxTask(this, SyncListener { this.openApplication() }).execute()
    }

    private fun showLoginError() {
        val builder = AlertDialog.Builder(this)
        builder.setMessage(getString(R.string.failed_to_login))
        builder.setPositiveButton(R.string.ok) { dialogInterface, i -> dialogInterface.dismiss() }
        builder.create().show()
    }

    private fun initButtons() {
        binding!!.googleButton.setOnClickListener { view -> googleLoginClick() }
        binding!!.localButton.setOnClickListener { view -> restoreLocalData() }
        binding!!.dropboxButton.setOnClickListener { view -> loginToDropbox() }
        binding!!.skipButton.setOnClickListener { view -> askForBirthdays() }
    }

    private fun askForBirthdays() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(getString(R.string.import_birthdays))
        builder.setMessage(getString(R.string.would_you_like_to_import_birthdays))
        builder.setPositiveButton(getString(R.string.import_string)) { dialogInterface, i ->
            dialogInterface.dismiss()
            importBirthdays()
        }
        builder.setNegativeButton(getString(R.string.open_app)) { dialogInterface, i ->
            dialogInterface.dismiss()
            openApplication()
        }
        builder.create().show()
    }

    private fun importBirthdays() {
        if (!Permissions.checkPermission(this, Permissions.READ_CONTACTS)) {
            Permissions.requestPermission(this, PERM_BIRTH, Permissions.READ_CONTACTS)
            return
        }
        Prefs.getInstance(this).isContactBirthdaysEnabled = true
        Prefs.getInstance(this).isBirthdayReminderEnabled = true
        CheckBirthdaysAsync(this, true, TaskCallback { this.openApplication() }).execute()
    }

    private fun initGroups() {
        if (AppDb.getAppDatabase(this).groupDao().all.size == 0) {
            GroupsUtil.initDefault(this)
        }
    }

    private fun loginToDropbox() {
        if (Permissions.checkPermission(this, Permissions.READ_EXTERNAL, Permissions.WRITE_EXTERNAL)) {
            dropboxLogin!!.login()
        } else {
            Permissions.requestPermission(this, PERM_DROPBOX, Permissions.READ_EXTERNAL, Permissions.WRITE_EXTERNAL)
        }
    }

    private fun restoreLocalData() {
        if (!Permissions.checkPermission(this, Permissions.READ_EXTERNAL, Permissions.WRITE_EXTERNAL)) {
            Permissions.requestPermission(this, PERM_LOCAL, Permissions.READ_EXTERNAL, Permissions.WRITE_EXTERNAL)
            return
        }
        RestoreLocalTask(this, RestoreLocalTask.SyncListener { this.openApplication() }).execute()
    }

    private fun loadDataFromGoogle() {
        RestoreGoogleTask(this, SyncListener { this.loadGoogleTasks() }).execute()
    }

    private fun loadGoogleTasks() {
        GetTaskListAsync(this, object : TasksCallback {
            override fun onComplete() {
                openApplication()
            }

            override fun onFailed() {
                openApplication()
            }
        }).execute()
    }

    private fun openApplication() {
        enableShortcuts()
        initGroups()
        Prefs.getInstance(this).isUserLogged = true
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    private fun enableShortcuts() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            val shortcutManager = getSystemService(ShortcutManager::class.java)

            val shortcut = ShortcutInfo.Builder(this, "id.reminder")
                    .setShortLabel(getString(R.string.add_reminder_menu))
                    .setLongLabel(getString(R.string.add_reminder_menu))
                    .setIcon(Icon.createWithResource(this, R.drawable.add_reminder_shortcut))
                    .setIntents(arrayOf(Intent(Intent.ACTION_MAIN).setClass(this, MainActivity::class.java), Intent(Intent.ACTION_VIEW).setClass(this, CreateReminderActivity::class.java)))
                    .build()

            val shortcut2 = ShortcutInfo.Builder(this, "id.note")
                    .setShortLabel(getString(R.string.add_note))
                    .setLongLabel(getString(R.string.add_note))
                    .setIcon(Icon.createWithResource(this, R.drawable.add_note_shortcut))
                    .setIntents(arrayOf(Intent(Intent.ACTION_MAIN).setClass(this, MainActivity::class.java), Intent(Intent.ACTION_VIEW).setClass(this, CreateNoteActivity::class.java)))
                    .build()
            if (shortcutManager != null) {
                shortcutManager.dynamicShortcuts = Arrays.asList(shortcut, shortcut2)
            }
        }
    }

    private fun googleLoginClick() {
        if (Permissions.checkPermission(this, Permissions.GET_ACCOUNTS, Permissions.READ_EXTERNAL,
                        Permissions.WRITE_EXTERNAL)) {
            googleLogin!!.login()
        } else {
            Permissions.requestPermission(this, PERM, Permissions.GET_ACCOUNTS,
                    Permissions.READ_EXTERNAL, Permissions.WRITE_EXTERNAL)
        }
    }

    private fun initCheckbox() {
        setViewHTML(binding!!.termsCheckBox, getString(R.string.i_accept))
        binding!!.termsCheckBox.setOnCheckedChangeListener { compoundButton, b -> setEnabling(b) }
        binding!!.termsCheckBox.isChecked = true
    }

    private fun setEnabling(b: Boolean) {
        binding!!.dropboxButton.isEnabled = b
        binding!!.googleButton.isEnabled = b
        binding!!.localButton.isEnabled = b
        binding!!.skipButton.isEnabled = b
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
        builder.setPositiveButton(R.string.ok) { dialogInterface, i -> dialogInterface.dismiss() }
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
        googleLogin!!.onActivityResult(requestCode, resultCode, data)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            PERM -> googleLoginClick()
            PERM_DROPBOX -> loginToDropbox()
            PERM_LOCAL -> restoreLocalData()
            PERM_BIRTH -> importBirthdays()
        }
    }

    companion object {

        private val PERM = 103
        private val PERM_DROPBOX = 104
        private val PERM_LOCAL = 105
        private val PERM_BIRTH = 106
        private val TERMS_URL = "termsopen.com"
    }
}
