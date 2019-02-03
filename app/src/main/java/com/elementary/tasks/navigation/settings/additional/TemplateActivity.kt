package com.elementary.tasks.navigation.settings.additional

import android.content.ContentResolver
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.elementary.tasks.R
import com.elementary.tasks.ReminderApp
import com.elementary.tasks.core.ThemedActivity
import com.elementary.tasks.core.data.models.SmsTemplate
import com.elementary.tasks.core.utils.*
import com.elementary.tasks.core.view_models.Commands
import com.elementary.tasks.core.view_models.sms_templates.SmsTemplateViewModel
import com.elementary.tasks.databinding.ActivityTemplateBinding
import javax.inject.Inject

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
class TemplateActivity : ThemedActivity<ActivityTemplateBinding>() {

    private lateinit var viewModel: SmsTemplateViewModel
    private var mItem: SmsTemplate? = null
    private var mUri: Uri? = null

    @Inject
    lateinit var backupTool: BackupTool

    init {
        ReminderApp.appComponent.inject(this)
    }

    override fun layoutRes(): Int = R.layout.activity_template

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initActionBar()
        loadTemplate()
    }

    private fun loadTemplate() {
        val intent = intent
        val id = intent.getStringExtra(Constants.INTENT_ID) ?: ""
        initViewModel(id)
        if (intent.data != null) {
            mUri = intent.data
            readUri()
        } else if (intent.hasExtra(Constants.INTENT_ITEM)) {
            try {
                val item = intent.getSerializableExtra(Constants.INTENT_ITEM) as SmsTemplate?
                if (item != null) {
                    showTemplate(item)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun readUri() {
        if (!Permissions.ensurePermissions(this, SD_REQ, Permissions.READ_EXTERNAL)) {
            return
        }
        mUri?.let {
            try {
                val scheme = it.scheme
                mItem = if (ContentResolver.SCHEME_CONTENT != scheme) {
                    backupTool.getTemplate(it.path, null)
                } else null
                mItem?.let { item -> showTemplate(item) }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun initViewModel(id: String) {
        viewModel = ViewModelProviders.of(this, SmsTemplateViewModel.Factory(id))
                .get(SmsTemplateViewModel::class.java)
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

    private fun showTemplate(smsTemplate: SmsTemplate) {
        this.mItem = smsTemplate
        binding.toolbar.title = getString(R.string.edit_template)
        if (!viewModel.isEdited) {
            binding.messageInput.setText(smsTemplate.title)
            viewModel.isEdited = true
        }
    }

    private fun initActionBar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        binding.toolbar.navigationIcon = ViewUtils.backIcon(this, isDark)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_add -> {
                saveTemplate()
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

    private fun deleteItem() {
        mItem?.let { viewModel.deleteSmsTemplate(it) }
    }

    private fun saveTemplate() {
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
        viewModel.saveTemplate(item)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_create_template, menu)
        if (mItem != null) {
            menu.add(Menu.NONE, MENU_ITEM_DELETE, 100, getString(R.string.delete))
        }
        return true
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == SD_REQ && Permissions.isAllGranted(grantResults)) {
            readUri()
        }
    }

    companion object {
        private const val MENU_ITEM_DELETE = 12
        private const val SD_REQ = 555
    }
}
