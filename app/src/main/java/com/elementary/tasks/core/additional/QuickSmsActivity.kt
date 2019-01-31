package com.elementary.tasks.core.additional

import android.app.Activity
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.telephony.SmsManager
import android.view.WindowManager
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.elementary.tasks.R
import com.elementary.tasks.core.ThemedActivity
import com.elementary.tasks.core.binding.activities.QuickSmsActivityBinding
import com.elementary.tasks.core.data.models.SmsTemplate
import com.elementary.tasks.core.services.SendReceiver
import com.elementary.tasks.core.utils.Constants
import com.elementary.tasks.core.utils.Contacts
import com.elementary.tasks.core.utils.Permissions
import com.elementary.tasks.core.view_models.sms_templates.SmsTemplatesViewModel

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
@Suppress("DEPRECATION")
class QuickSmsActivity : ThemedActivity<QuickSmsActivityBinding>() {

    private var mAdapter: SelectableTemplatesAdapter = SelectableTemplatesAdapter()
    private var number: String = ""

    override fun layoutRes(): Int = R.layout.activity_quick_sms

    override fun newBinding(activity: Activity): QuickSmsActivityBinding = QuickSmsActivityBinding(activity)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        number = intent.getStringExtra(Constants.SELECTED_CONTACT_NUMBER) ?: ""

        binding.messagesList.layoutManager = LinearLayoutManager(this)
        binding.messagesList.adapter = mAdapter

        binding.buttonSend.setOnClickListener { startSending() }
        val name = Contacts.getNameFromNumber(number, this) ?: ""
        binding.contactInfo.text = "$name\n$number"

        initViewModel()
    }

    private fun initViewModel() {
        val viewModel = ViewModelProviders.of(this).get(SmsTemplatesViewModel::class.java)
        viewModel.smsTemplates.observe(this, Observer { smsTemplates ->
            if (smsTemplates != null) {
                updateList(smsTemplates)
            }
        })
    }

    private fun updateList(smsTemplates: List<SmsTemplate>) {
        mAdapter.setData(smsTemplates)
        if (mAdapter.itemCount > 0) {
            mAdapter.selectItem(0)
        }
    }

    private fun startSending() {
        if (!Permissions.ensurePermissions(this, REQ_SMS, Permissions.SEND_SMS)) {
            return
        }
        val position = mAdapter.selectedPosition
        val item = mAdapter.getItem(position)
        if (item != null) {
            sendSMS(number, item.title)
        } else {
            sendError()
        }
    }

    private fun removeFlags() {
        window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                or WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                or WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                or WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD)
        finish()
    }

    private fun sendSMS(number: String?, message: String?) {
        val action = "SMS_SENT"
        val sentPI = PendingIntent.getBroadcast(this, 0, Intent(action), 0)
        registerReceiver(SendReceiver { b ->
            if (b) {
                removeFlags()
            } else {
                sendError()
            }
        }, IntentFilter(action))
        val sms = SmsManager.getDefault()
        try {
            sms.sendTextMessage(number, null, message, sentPI, null)
        } catch (e: SecurityException) {
            sendError()
        }
    }

    private fun sendError() {
        Toast.makeText(this, R.string.error_sending, Toast.LENGTH_SHORT).show()
    }

    override fun onBackPressed() {
        removeFlags()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQ_SMS -> if (Permissions.isAllGranted(grantResults)) {
                startSending()
            }
        }
    }

    companion object {

        private const val REQ_SMS = 425

        fun openScreen(context: Context, number: String) {
            context.startActivity(Intent(context, QuickSmsActivity::class.java)
                    .putExtra(Constants.SELECTED_CONTACT_NUMBER, number)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        }
    }
}
