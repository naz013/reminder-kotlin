package com.elementary.tasks.core.additional

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.elementary.tasks.R
import com.elementary.tasks.core.arch.BindingActivity
import com.elementary.tasks.core.data.models.SmsTemplate
import com.elementary.tasks.core.utils.Constants
import com.elementary.tasks.core.utils.Contacts
import com.elementary.tasks.core.utils.TelephonyUtil
import com.elementary.tasks.core.view_models.sms_templates.SmsTemplatesViewModel
import com.elementary.tasks.databinding.ActivityQuickSmsBinding

class QuickSmsActivity : BindingActivity<ActivityQuickSmsBinding>(R.layout.activity_quick_sms) {

    private var mAdapter: SelectableTemplatesAdapter = SelectableTemplatesAdapter()
    private var number: String = ""

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
        if (number != null) {
            TelephonyUtil.sendSms(this, number, message)
        } else {
            sendError()
        }
    }

    private fun sendError() {
        Toast.makeText(this, R.string.error_sending, Toast.LENGTH_SHORT).show()
    }

    override fun onBackPressed() {
        removeFlags()
    }

    companion object {

        fun openScreen(context: Context, number: String) {
            context.startActivity(Intent(context, QuickSmsActivity::class.java)
                    .putExtra(Constants.SELECTED_CONTACT_NUMBER, number)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        }
    }
}
