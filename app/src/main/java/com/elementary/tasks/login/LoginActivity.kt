package com.elementary.tasks.login

import android.content.Intent
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
import android.widget.CheckBox
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.elementary.tasks.R
import com.elementary.tasks.core.ThemedActivity
import com.elementary.tasks.core.app_widgets.UpdatesHelper
import com.elementary.tasks.core.cloud.DropboxLogin
import com.elementary.tasks.core.cloud.GDrive
import com.elementary.tasks.core.cloud.GoogleLogin
import com.elementary.tasks.core.data.AppDb
import com.elementary.tasks.core.utils.*
import com.elementary.tasks.databinding.ActivityLoginBinding
import com.elementary.tasks.groups.GroupsUtil
import com.elementary.tasks.intro.PageFragment
import com.elementary.tasks.intro.PrivacyPolicyActivity
import com.elementary.tasks.navigation.MainActivity
import com.elementary.tasks.notes.create.CreateNoteActivity
import com.elementary.tasks.reminder.create.CreateReminderActivity
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import java.util.*

class LoginActivity : ThemedActivity<ActivityLoginBinding>() {

    private lateinit var viewModel: LoginViewModel

    private var googleLogin: GoogleLogin? = null
    private var dropboxLogin: DropboxLogin? = null

    private var scrollJob: Job? = null

    override fun layoutRes(): Int = R.layout.activity_login

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(LoginViewModel::class.java)

        if (Module.isPro) binding.appNameBannerPro.visibility = View.VISIBLE
        else binding.appNameBannerPro.visibility = View.GONE
        if (SuperUtil.isGooglePlayServicesAvailable(this)) {
            googleLogin = GoogleLogin(this, prefs)
        }
        dropboxLogin = DropboxLogin(this, object : DropboxLogin.LoginCallback {
            override fun onSuccess(b: Boolean) {
                if (b) viewModel.loadDataFromDropbox()
            }
        })

        val mPagerAdapter = SlidePagerAdapter(supportFragmentManager)
        binding.viewPager.adapter = mPagerAdapter
        binding.indicator.setViewPager(binding.viewPager)

        initButtons()
        initCheckbox()
    }

    private fun initCheckbox() {
        setViewHTML(binding.termsCheckBox, getString(R.string.i_accept))
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
        startActivity(Intent(this, PrivacyPolicyActivity::class.java))
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

    override fun onStart() {
        super.onStart()
        observeStates()
    }

    override fun onBackPressed() {
        viewModel.onBack()
        super.onBackPressed()
    }

    private fun observeStates() {
        viewModel.isLoading.observe(this, Observer { isLoading ->
            isLoading?.let {
                if (it) binding.progressView.visibility = View.VISIBLE
                else binding.progressView.visibility = View.INVISIBLE
                setEnabling(!it)
            }
        })
        viewModel.isReady.observe(this, Observer { isReady ->
            isReady?.let {
                if (it) finishRestoring()
            }
        })
        viewModel.message.observe(this, Observer {
            if (it != null && it != 0) {
                binding.progressMessageView.text = getString(it)
            }
        })
    }

    override fun onResume() {
        super.onResume()
        dropboxLogin?.checkDropboxStatus()
        startScroll()
    }

    private fun showNextSlide() {
        val current = binding.viewPager.currentItem
        if (current in 0..2) {
            binding.viewPager.currentItem = current + 1
        } else {
            binding.viewPager.currentItem = 0
        }
    }

    private fun startScroll() {
        scrollJob = launchDefault {
            while (true) {
                delay(2000)
                withUIContext { showNextSlide() }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        scrollJob?.cancel()
    }

    private fun finishRestoring() {
        UpdatesHelper.updateWidget(this)
        UpdatesHelper.updateNotesWidget(this)
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
            binding.googleButton.visibility = View.VISIBLE
            binding.googleButton.setOnClickListener { verifyTerms { googleLoginClick() } }
        } else {
            binding.googleButton.visibility = View.GONE
        }
        binding.localButton.setOnClickListener { verifyTerms { restoreLocalData() } }
        binding.dropboxButton.setOnClickListener { verifyTerms { loginToDropbox() } }
        binding.skipButton.setOnClickListener { verifyTerms { openApplication() } }
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
        viewModel.loadDataFromLocal()
    }

    private fun verifyTerms(onSuccess: () -> Unit) {
        if (!binding.termsCheckBox.isChecked) {
            Toast.makeText(this, getString(R.string.privacy_warming), Toast.LENGTH_SHORT).show()
            return
        }
        onSuccess.invoke()
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
                    if (isLogged) viewModel.loadDataFromGoogle()
                    else showLoginError()
                }

                override fun onFail() {
                    showLoginError()
                }
            })
        }
    }

    private fun setEnabling(b: Boolean) {
        binding.dropboxButton.isEnabled = b
        binding.googleButton.isEnabled = b
        binding.localButton.isEnabled = b
        binding.skipButton.isEnabled = b
        binding.termsCheckBox.isEnabled = b
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

    private inner class SlidePagerAdapter internal constructor(fm: FragmentManager) : FragmentPagerAdapter(fm) {
        override fun getItem(position: Int): Fragment {
            return PageFragment.newInstance(position)
        }
        override fun getCount(): Int {
            return 4
        }
    }

    companion object {
        private const val PERM = 103
        private const val PERM_DROPBOX = 104
        private const val PERM_LOCAL = 105
        private const val TERMS_URL = "termsopen.com"
    }
}
