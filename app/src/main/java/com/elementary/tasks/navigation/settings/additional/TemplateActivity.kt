package com.elementary.tasks.navigation.settings.additional

import android.content.ContentResolver
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.elementary.tasks.R
import com.elementary.tasks.core.arch.BindingActivity
import com.elementary.tasks.core.data.models.SmsTemplate
import com.elementary.tasks.core.utils.*
import com.elementary.tasks.core.view_models.Commands
import com.elementary.tasks.core.view_models.sms_templates.SmsTemplateViewModel
import com.elementary.tasks.databinding.ActivityTemplateBinding
import java.util.*

class TemplateActivity : BindingActivity<ActivityTemplateBinding>(R.layout.activity_template) {

    private val viewModel: SmsTemplateViewModel by lazy {
        ViewModelProviders.of(this, SmsTemplateViewModel.Factory(getId()))
                .get(SmsTemplateViewModel::class.java)
    }
    private var mItem: SmsTemplate? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initActionBar()
        loadTemplate()
    }

    private fun getId(): String = intent.getStringExtra(Constants.INTENT_ID) ?: ""

    private fun loadTemplate() {
        val intent = intent
        initViewModel()
        if (intent.data != null) {
            readUri()
        } else if (intent.hasExtra(Constants.INTENT_ITEM)) {
            try {
                val item = intent.getParcelableExtra(Constants.INTENT_ITEM) as SmsTemplate?
                if (item != null) {
                    showTemplate(item, true)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun readUri() {
        if (!Permissions.checkPermission(this, SD_REQ, Permissions.READ_EXTERNAL)) {
            return
        }
        intent.data?.let {
            try {
                mItem = if (ContentResolver.SCHEME_CONTENT != it.scheme) {
                    val any = MemoryUtil.readFromUri(this, it)
                    if (any != null && any is SmsTemplate) {
                        any
                    } else null
                } else null
                mItem?.let { item -> showTemplate(item, true) }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun initViewModel() {
        viewModel.smsTemplate.observe(this, Observer{ smsTemplate ->
            if (smsTemplate != null) {
                showTemplate(smsTemplate)
            }
        })
        viewModel.result.observe(this, Observer{ commands ->
            if (commands != null) {
                when (commands) {
                    Commands.SAVED, Commands.DELETED -> finish()
                    else -> {
                    }
                }
            }
        })
    }

    private fun showTemplate(smsTemplate: SmsTemplate, fromFile: Boolean = false) {
        this.mItem = smsTemplate
        binding.toolbar.title = getString(R.string.edit_template)
        if (!viewModel.isEdited) {
            binding.messageInput.setText(smsTemplate.title)
            viewModel.isEdited = true
            viewModel.isFromFile = fromFile
            if (fromFile) {
                viewModel.findSame(smsTemplate.key)
            }
        }
    }

    private fun initActionBar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        binding.toolbar.navigationIcon = ViewUtils.backIcon(this, isDarkMode)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_add -> {
                askCopySaving()
                return true
            }
            android.R.id.home -> {
                finish()
                return true
            }
            MENU_ITEM_DELETE -> {
                deleteItem()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun askCopySaving() {
        if (viewModel.isFromFile && viewModel.hasSameInDb) {
            dialogues.getMaterialDialog(this)
                    .setMessage(R.string.same_template_message)
                    .setPositiveButton(R.string.keep) { dialogInterface, _ ->
                        dialogInterface.dismiss()
                        saveTemplate(true)
                    }
                    .setNegativeButton(R.string.replace) { dialogInterface, _ ->
                        dialogInterface.dismiss()
                        saveTemplate()
                    }
                    .setNeutralButton(R.string.cancel) { dialogInterface, _ ->
                        dialogInterface.dismiss()
                    }
                    .create()
                    .show()
        } else {
            saveTemplate()
        }
    }

    private fun deleteItem() {
        mItem?.let { viewModel.deleteSmsTemplate(it) }
    }

    private fun saveTemplate(newId: Boolean = false) {
        val text = binding.messageInput.text.toString().trim()
        if (text.isEmpty()) {
            binding.messageLayout.error = getString(R.string.must_be_not_empty)
            binding.messageLayout.isErrorEnabled = true
            return
        }
        val date = TimeUtil.gmtDateTime
        val item = (mItem ?: SmsTemplate()).apply {
            this.date = date
            this.title = text
        }
        if (newId) {
            item.key = UUID.randomUUID().toString()
        }
        viewModel.saveTemplate(item)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_create_template, menu)
        if (mItem != null && !viewModel.isFromFile) {
            menu.add(Menu.NONE, MENU_ITEM_DELETE, 100, getString(R.string.delete))
        }
        return true
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == SD_REQ && Permissions.checkPermission(grantResults)) {
            readUri()
        }
    }

    companion object {
        private const val MENU_ITEM_DELETE = 12
        private const val SD_REQ = 555
    }
}
