package com.elementary.tasks.core.additional

import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.telephony.SmsManager
import android.view.WindowManager
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.elementary.tasks.R
import com.elementary.tasks.core.ThemedActivity
import com.elementary.tasks.core.data.models.SmsTemplate
import com.elementary.tasks.core.utils.Constants
import com.elementary.tasks.core.utils.Contacts
import com.elementary.tasks.core.utils.LogUtil
import com.elementary.tasks.core.utils.Permissions
import com.elementary.tasks.core.viewModels.smsTemplates.SmsTemplatesViewModel
import kotlinx.android.synthetic.main.activity_quick_sms.*

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
class QuickSmsActivity : ThemedActivity() {

    private var mAdapter: SelectableTemplatesAdapter = SelectableTemplatesAdapter()

    private var number: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initData()
        setContentView(R.layout.activity_quick_sms)

        messagesList.layoutManager = LinearLayoutManager(this)
        messagesList.adapter = mAdapter

        buttonSend.setOnClickListener { startSending() }
        val name = Contacts.getNameFromNumber(number, this)
        contactInfo.text = "$name\n$number"

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

    private fun initData() {
        number = intent.getStringExtra(Constants.SELECTED_CONTACT_NUMBER)
    }

    private fun startSending() {
        if (!Permissions.checkPermission(this, Permissions.SEND_SMS)) {
            Permissions.requestPermission(this, REQ_SMS, Permissions.SEND_SMS)
            return
        }
        val position = mAdapter.selectedPosition
        val item = mAdapter.getItem(position)
        if (item != null) {
            LogUtil.d("TAG", "startSending: " + item.title)
            sendSMS(number, item.title)
        }
        removeFlags()
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
        val s = "SMS_DELIVERED"
        val sentPI = PendingIntent.getBroadcast(this, 0, Intent(action), 0)
        val deliveredPI = PendingIntent.getBroadcast(this, 0, Intent(s), 0)
        val sms = SmsManager.getDefault()
        sms.sendTextMessage(number, null, message, sentPI, deliveredPI)
    }

    override fun onBackPressed() {
        removeFlags()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults.isEmpty()) return
        when (requestCode) {
            REQ_SMS -> if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startSending()
            }
        }
    }

    companion object {

        private const val REQ_SMS = 425
    }
}
