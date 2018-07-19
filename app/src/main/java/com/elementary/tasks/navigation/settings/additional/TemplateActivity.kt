package com.elementary.tasks.navigation.settings.additional

import android.content.ContentResolver
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Menu
import android.view.MenuItem
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.elementary.tasks.R
import com.elementary.tasks.core.ThemedActivity
import com.elementary.tasks.core.data.models.SmsTemplate
import com.elementary.tasks.core.utils.BackupTool
import com.elementary.tasks.core.utils.Constants
import com.elementary.tasks.core.utils.TimeUtil
import com.elementary.tasks.core.viewModels.Commands
import com.elementary.tasks.core.viewModels.smsTemplates.SmsTemplateViewModel
import kotlinx.android.synthetic.main.activity_template.*
import java.io.IOException

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

    private lateinit var viewModel: SmsTemplateViewModel

    private var mItem: SmsTemplate? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_template)
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
                mItem = if (ContentResolver.SCHEME_CONTENT == scheme) {
                    val cr = contentResolver
                    BackupTool.getInstance().getTemplate(cr, name)
                } else {
                    BackupTool.getInstance().getTemplate(name.path, null)
                }
            } catch (e: IOException) {
                e.printStackTrace()
            } catch (e: IllegalStateException) {
                e.printStackTrace()
            }

        }
    }

    private fun initViewModel(id: String) {
        viewModel = ViewModelProviders.of(this, SmsTemplateViewModel.Factory(application, id))
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
                }
            }
        })
    }

    private fun initMessageField() {
        messageInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                updateLeftView(s.length)
            }

            override fun afterTextChanged(s: Editable) {

            }
        })
    }

    private fun updateLeftView(count: Int) {
        leftCharacters.text = String.format(getString(R.string.left_characters_x), (120 - count).toString() + "")
    }

    private fun showTemplate(smsTemplate: SmsTemplate) {
        this.mItem = smsTemplate
        messageInput.setText(smsTemplate.title)
        val title = smsTemplate.title
        if (title != "") {
            updateLeftView(title.length)
        }
    }

    private fun initActionBar() {
        setSupportActionBar(toolbar)
        if (supportActionBar != null) {
            supportActionBar!!.setDisplayShowTitleEnabled(false)
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
            supportActionBar!!.setHomeButtonEnabled(true)
            supportActionBar!!.setDisplayShowHomeEnabled(true)
        }
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp)
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
            viewModel.deleteSmsTemplate(mItem!!)
        }
    }

    private fun saveTemplate() {
        val text = messageInput.text!!.toString().trim { it <= ' ' }
        if (text.isEmpty()) {
            messageInput.error = getString(R.string.must_be_not_empty)
            return
        }
        val date = TimeUtil.gmtDateTime
        var item = mItem
        if (item != null) {
            item.date = date
            item.title = text
        } else {
            item = SmsTemplate(text, date)
        }
        viewModel.saveTemplate(item)
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

        private const val MENU_ITEM_DELETE = 12
    }
}
