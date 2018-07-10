package com.elementary.tasks.core.additional

import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.telephony.SmsManager
import android.view.WindowManager

import com.elementary.tasks.R
import com.elementary.tasks.core.ThemedActivity
import com.elementary.tasks.core.data.models.SmsTemplate
import com.elementary.tasks.core.utils.Constants
import com.elementary.tasks.core.utils.Contacts
import com.elementary.tasks.core.utils.LogUtil
import com.elementary.tasks.core.utils.Permissions
import com.elementary.tasks.core.utils.SuperUtil
import com.elementary.tasks.core.view_models.sms_templates.SmsTemplatesViewModel
import com.elementary.tasks.databinding.ActivityQuickSmsBinding
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager

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

    private var mAdapter: SelectableTemplatesAdapter? = null

    private var number: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initData()
        val binding = DataBindingUtil.setContentView<ActivityQuickSmsBinding>(this, R.layout.activity_quick_sms)

        binding.messagesList.layoutManager = LinearLayoutManager(this)
        mAdapter = SelectableTemplatesAdapter(this)
        binding.messagesList.adapter = mAdapter

        binding.buttonSend.setOnClickListener { v -> startSending() }
        val name = Contacts.getNameFromNumber(number, this)
        binding.contactInfo.text = SuperUtil.appendString(name, "\n", number)

        initViewModel()
    }

    private fun initViewModel() {
        val viewModel = ViewModelProviders.of(this).get(SmsTemplatesViewModel::class.java)
        viewModel.smsTemplates.observe(this, { smsTemplates ->
            if (smsTemplates != null) {
                updateList(smsTemplates)
            }
        })
    }

    private fun updateList(smsTemplates: List<SmsTemplate>?) {
        mAdapter!!.setData(smsTemplates)
        if (mAdapter!!.itemCount > 0) {
            mAdapter!!.selectItem(0)
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
        val position = mAdapter!!.selectedPosition
        val item = mAdapter!!.getItem(position)
        if (item != null) {
            LogUtil.d("TAG", "startSending: " + item.title!!)
            sendSMS(number, item.title)
        }
        removeFlags()
    }

    fun removeFlags() {
        window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                or WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                or WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                or WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD)
        finish()
    }

    private fun sendSMS(number: String?, message: String?) {
        val SENT = "SMS_SENT"
        val DELIVERED = "SMS_DELIVERED"
        val sentPI = PendingIntent.getBroadcast(this, 0, Intent(SENT), 0)
        val deliveredPI = PendingIntent.getBroadcast(this, 0, Intent(DELIVERED), 0)
        val sms = SmsManager.getDefault()
        sms.sendTextMessage(number, null, message, sentPI, deliveredPI)
    }

    override fun onBackPressed() {
        removeFlags()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults.size == 0) return
        when (requestCode) {
            REQ_SMS -> if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startSending()
            }
        }
    }

    companion object {

        private val REQ_SMS = 425
    }
}
