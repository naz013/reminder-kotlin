package com.elementary.tasks.sms

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.LinearLayoutManager
import com.elementary.tasks.R
import com.elementary.tasks.core.arch.BindingActivity
import com.elementary.tasks.core.data.ui.sms.UiSmsList
import com.elementary.tasks.core.utils.Constants
import com.elementary.tasks.core.utils.TelephonyUtil
import com.elementary.tasks.core.utils.nonNullObserve
import com.elementary.tasks.core.utils.toast
import com.elementary.tasks.databinding.ActivityQuickSmsBinding
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber

@Deprecated("After S")
class QuickSmsActivity : BindingActivity<ActivityQuickSmsBinding>() {

  private val viewModel by viewModel<QuickSmsViewModel>()
  private var selectableTemplatesAdapter = SelectableTemplatesAdapter()

  override fun inflateBinding() = ActivityQuickSmsBinding.inflate(layoutInflater)

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    binding.messagesList.layoutManager = LinearLayoutManager(this)
    binding.messagesList.adapter = selectableTemplatesAdapter

    binding.buttonSend.setOnClickListener { startSending() }

    viewModel.loadContactInfo(intentString(Constants.SELECTED_CONTACT_NUMBER, ""))
    initViewModel()
  }

  private fun initViewModel() {
    viewModel.contactInfo.nonNullObserve(this) {
      binding.contactInfo.text = it
    }
    viewModel.smsTemplates.nonNullObserve(this) { smsTemplates ->
      updateList(smsTemplates)
    }
    lifecycle.addObserver(viewModel)
  }

  private fun updateList(smsTemplates: List<UiSmsList>) {
    selectableTemplatesAdapter.submitList(smsTemplates)
    if (selectableTemplatesAdapter.itemCount > 0) {
      selectableTemplatesAdapter.selectItem(0)
    }
  }

  private fun startSending() {
    val item = selectableTemplatesAdapter.getSelectedItem()
    Timber.d("startSending: $item")
    if (item != null) {
      sendSMS(viewModel.number, item.text)
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
    toast(R.string.error_sending)
  }

  override fun handleBackPress(): Boolean {
    removeFlags()
    return true
  }

  companion object {

    fun openScreen(context: Context, number: String) {
      context.startActivity(Intent(context, QuickSmsActivity::class.java)
        .putExtra(Constants.SELECTED_CONTACT_NUMBER, number)
        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }
  }
}
