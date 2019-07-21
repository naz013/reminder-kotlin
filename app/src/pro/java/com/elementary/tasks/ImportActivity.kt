package com.elementary.tasks

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.InputFilter
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.elementary.tasks.core.arch.BindingActivity
import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.core.utils.Constants
import com.elementary.tasks.core.utils.ViewUtils
import com.elementary.tasks.databinding.ActivityImportSharingBinding
import com.elementary.tasks.reminder.create.CreateReminderActivity

class ImportActivity : BindingActivity<ActivityImportSharingBinding>(R.layout.activity_import_sharing) {

    private val viewModel: ShareViewModel by lazy {
        ViewModelProviders.of(this).get(ShareViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initActionBar()

        binding.shareButton.isEnabled = false
        binding.shareButton.setOnClickListener { importClick() }
        binding.autoCheck.isChecked = prefs.isAutoImportSharedData
        binding.autoCheck.setOnCheckedChangeListener { _, isChecked -> prefs.isAutoImportSharedData = isChecked }
        binding.codeField.filters = arrayOf(InputFilter.AllCaps())

        val data = intent.dataString
        if (data != null) {
            readUri(data)
        }

        initViewModel()
    }

    private fun initViewModel() {
        viewModel.isLogged.observe(this, Observer {
            if (it != null) {
                binding.shareButton.isEnabled = it
            }
        })
        viewModel.isError.observe(this, Observer {
            if (it != null && it) showError()
        })
        viewModel.isLoading.observe(this, Observer {
            if (it != null) {
                updateProgress(it)
            }
        })
        viewModel.isSuccess.observe(this, Observer {
            if (it != null && it) {
                Toast.makeText(this, getString(R.string.reminder_imported_success), Toast.LENGTH_SHORT).show()
            }
        })
        viewModel.reminder.observe(this, Observer {
            if (it != null) {
                editReminder(it)
            }
        })
    }

    private fun updateProgress(b: Boolean) {
        if (b) {
            binding.progressView.visibility = View.VISIBLE
        } else {
            binding.progressView.visibility = View.GONE
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
        binding.toolbar.navigationIcon = ViewUtils.backIcon(this, isDarkMode)
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

        viewModel.read(key, password)
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

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
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