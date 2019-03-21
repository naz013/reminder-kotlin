package com.elementary.tasks

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import com.elementary.tasks.core.ThemedActivity
import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.core.utils.Constants
import com.elementary.tasks.core.utils.ViewUtils
import com.elementary.tasks.core.utils.launchDefault
import com.elementary.tasks.core.utils.withUIContext
import com.elementary.tasks.databinding.ActivityImportSharingBinding
import com.elementary.tasks.reminder.create.CreateReminderActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.gson.Gson
import org.koin.android.ext.android.inject
import timber.log.Timber

class ImportActivity : ThemedActivity<ActivityImportSharingBinding>() {

    private val qrShareProvider: QrShareProvider by inject()

    private lateinit var auth: FirebaseAuth

    override fun layoutRes(): Int = R.layout.activity_import_sharing

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initActionBar()

        binding.shareButton.isEnabled = false
        binding.shareButton.setOnClickListener { importClick() }

        val data = intent.dataString
        if (data != null) {
            readUri(data)
        }

        auth = FirebaseAuth.getInstance()
    }

    override fun onStart() {
        super.onStart()
        val currentUser = auth.currentUser
        updateUI(currentUser)
        if (currentUser == null) {
            login()
        }
    }

    private fun updateUI(currentUser: FirebaseUser?) {
        binding.shareButton.isEnabled = currentUser != null
    }

    private fun login() {
        auth.signInAnonymously()
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        Timber.d("signInAnonymously:success")
                        val user = auth.currentUser
                        updateUI(user)
                    } else {
                        Timber.d("signInAnonymously:failure -> ${task.exception}")
                        updateUI(null)
                    }
                }
    }

    private fun readUri(data: String) {
        try {
            val uri = Uri.parse(data)
            binding.codeField.setText(uri.getQueryParameter("key"))
        } catch (e: Exception) {
        }
    }

    private fun initActionBar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        binding.toolbar.navigationIcon = ViewUtils.backIcon(this, isDark)
        binding.toolbar.title = getString(R.string.in_app_sharing)
    }

    private fun importClick() {
        val key = binding.codeField.text.toString().trim()
        if (key.isEmpty()) {
            binding.codeLayout.error = getString(R.string.must_be_not_empty)
            binding.codeLayout.isErrorEnabled = true
            return
        }

        val password = binding.passwordField.text.toString().trim()
        if (password.isEmpty()) {
            binding.passwordLayout.error = getString(R.string.must_be_not_empty)
            binding.passwordLayout.isErrorEnabled = true
            return
        }

        hideKeyboard()

        binding.passwordField.setText("")
        binding.codeField.setText("")

        qrShareProvider.readData(key.toUpperCase()) {
            if (it) {
                qrShareProvider.verifyData(password) { type, data ->
                    if (type != null && data != null) {
                        importData(type, data)
                    } else {
                        showError()
                    }
                }
            } else {
                showError()
            }
        }
    }

    private fun importData(type: String, data: String) {
        Timber.d("importData: $type, $data")
        when (type) {
            QrShareProvider.TYPE_REMINDER -> {
                importReminder(data)
            }
        }
    }

    private fun importReminder(data: String) {
        launchDefault {
            val json = QrShareProvider.readData(data)
            if (json != null) {
                val reminder = Gson().fromJson(json, Reminder::class.java)
                Timber.d("importReminder: $reminder")
                if (reminder != null && reminder.type != 0) {
                    withUIContext { editReminder(reminder) }
                }
            } else {
                showError()
            }
        }
    }

    private fun editReminder(reminder: Reminder) {
        val intent = Intent(this, CreateReminderActivity::class.java)
        intent.putExtra(Constants.INTENT_ITEM, reminder)
        startActivity(intent)
        finish()
    }

    private fun showError() {
        Toast.makeText(this, getString(R.string.failed_to_read_data), Toast.LENGTH_SHORT).show()
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (item != null) {
            when (item.itemId) {
                android.R.id.home -> {
                    finish()
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    companion object {

        fun newImportScreen(context: Context): Intent {
            return Intent(context, ImportActivity::class.java)
        }
    }
}