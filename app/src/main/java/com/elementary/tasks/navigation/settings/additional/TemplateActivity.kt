package com.elementary.tasks.navigation.settings.additional

import android.content.ContentResolver
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Menu
import android.view.MenuItem

import com.elementary.tasks.R
import com.elementary.tasks.core.ThemedActivity
import com.elementary.tasks.core.data.models.SmsTemplate
import com.elementary.tasks.core.utils.BackupTool
import com.elementary.tasks.core.utils.Constants
import com.elementary.tasks.core.utils.TimeUtil
import com.elementary.tasks.core.viewModels.sms_templates.SmsTemplateViewModel
import com.elementary.tasks.databinding.ActivityTemplateBinding

import java.io.IOException
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders

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
class TemplateActivity : ThemedActivity() {

    private var binding: ActivityTemplateBinding? = null
    private var viewModel: SmsTemplateViewModel? = null

    private var mItem: SmsTemplate? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_template)
        initActionBar()
        initMessageField()

        loadTemplate()
    }

    private fun loadTemplate() {
        val intent = intent
        val id = intent.getStringExtra(Constants.INTENT_ID)
        initViewModel(id)
        if (intent.data != null) {
            try {
                val name = intent.data
                val scheme = name!!.scheme
                if (ContentResolver.SCHEME_CONTENT == scheme) {
                    val cr = contentResolver
                    mItem = BackupTool.getInstance().getTemplate(cr, name)
                } else {
                    mItem = BackupTool.getInstance().getTemplate(name.path, null)
                }
            } catch (e: IOException) {
                e.printStackTrace()
            } catch (e: IllegalStateException) {
                e.printStackTrace()
            }

        }
    }

    private fun initViewModel(id: String) {
        viewModel = ViewModelProviders.of(this, SmsTemplateViewModel.Factory(application, id)).get(SmsTemplateViewModel::class.java)
        viewModel!!.smsTemplate.observe(this, { smsTemplate ->
            if (smsTemplate != null) {
                showTemplate(smsTemplate)
            }
        })
        viewModel!!.result.observe(this, { commands ->
            if (commands != null) {
                when (commands) {
                    Commands.SAVED, Commands.DELETED -> finish()
                }
            }
        })
    }

    private fun initMessageField() {
        binding!!.messageInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                updateLeftView(s.length)
            }

            override fun afterTextChanged(s: Editable) {

            }
        })
    }

    private fun updateLeftView(count: Int) {
        binding!!.leftCharacters.text = String.format(getString(R.string.left_characters_x), (120 - count).toString() + "")
    }

    private fun showTemplate(smsTemplate: SmsTemplate?) {
        this.mItem = smsTemplate
        if (smsTemplate != null) {
            binding!!.messageInput.setText(smsTemplate.title)
            val title = smsTemplate.title
            if (title != null) {
                updateLeftView(title.length)
            }
        }
    }

    private fun initActionBar() {
        setSupportActionBar(binding!!.toolbar)
        if (supportActionBar != null) {
            supportActionBar!!.setDisplayShowTitleEnabled(false)
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
            supportActionBar!!.setHomeButtonEnabled(true)
            supportActionBar!!.setDisplayShowHomeEnabled(true)
        }
        binding!!.toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp)
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
        if (mItem != null) {
            viewModel!!.deleteSmsTemplate(mItem!!)
        }
    }

    private fun saveTemplate() {
        val text = binding!!.messageInput.text!!.toString().trim { it <= ' ' }
        if (text.length == 0) {
            binding!!.messageInput.error = getString(R.string.must_be_not_empty)
            return
        }
        val date = TimeUtil.gmtDateTime
        if (mItem != null) {
            mItem!!.date = date
            mItem!!.title = text
        } else {
            mItem = SmsTemplate(text, date)
        }
        viewModel!!.saveTemplate(mItem!!)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_create_template, menu)
        if (mItem != null) {
            menu.add(Menu.NONE, MENU_ITEM_DELETE, 100, getString(R.string.delete))
        }
        return true
    }

    companion object {

        private val MENU_ITEM_DELETE = 12
    }
}
