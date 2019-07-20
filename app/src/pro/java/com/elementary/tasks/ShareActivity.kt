package com.elementary.tasks

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import com.elementary.tasks.core.arch.BindingActivity
import com.elementary.tasks.core.utils.ViewUtils
import com.elementary.tasks.databinding.ActivityShareBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import org.koin.android.ext.android.inject
import timber.log.Timber

class ShareActivity : BindingActivity<ActivityShareBinding>(R.layout.activity_share) {

    private val qrShareProvider: QrShareProvider by inject()
    private lateinit var auth: FirebaseAuth

    private var mData: String? = ""
    private var mType: String? = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initActionBar()

        binding.shareButton.isEnabled = false
        binding.shareButton.setOnClickListener { shareClick() }

        val data = intent.getStringExtra(ARG_DATA)
        val type = intent.getStringExtra(ARG_TYPE)
        if (data == null || type == null) {
            finish()
            return
        }
        mData = data
        mType = type

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

    private fun initActionBar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        binding.toolbar.navigationIcon = ViewUtils.backIcon(this, isDarkMode)
        binding.toolbar.title = getString(R.string.in_app_sharing)
    }

    private fun shareClick() {
        qrShareProvider.removeData()
        val password = binding.passwordField.text.toString().trim()
        if (password.isEmpty()) {
            binding.passwordLayout.error = getString(R.string.must_be_not_empty)
            binding.passwordLayout.isErrorEnabled = true
            return
        }

        clear()
        hideKeyboard()

        binding.passwordField.setText("")
        binding.shareButton.isEnabled = false

        val data = mData ?: return
        val type = mType ?: return

        qrShareProvider.shareData(type, data, password) {
            if (it != null) {
                binding.shareCodeView.text = it
                qrShareProvider.showQrImage(binding.shareCodeImage, it)
                binding.shareButton.isEnabled = true
            } else {
                showError()
            }
        }
    }

    private fun showError() {
        binding.shareButton.isEnabled = true
        Toast.makeText(this, getString(R.string.failed_to_share_data), Toast.LENGTH_SHORT).show()
    }

    private fun clear() {
        binding.shareCodeView.text = ""
        binding.shareCodeImage.setImageDrawable(null)
    }

    override fun onStop() {
        super.onStop()
        clear()
        qrShareProvider.removeData()
    }

    override fun onDestroy() {
        super.onDestroy()
        clear()
        qrShareProvider.removeData()
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
        private const val ARG_TYPE = "arg_type"
        private const val ARG_DATA = "arg_data"

        fun newShareScreen(context: Context, data: String, type: String): Intent {
            val intent = Intent(context, ShareActivity::class.java)
            intent.putExtra(ARG_DATA, data)
            intent.putExtra(ARG_TYPE, type)
            return intent
        }
    }
}