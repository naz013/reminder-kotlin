package com.elementary.tasks.birthdays.dialog

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.compose.runtime.Composable
import com.elementary.tasks.core.os.PermissionFlowDelegateImpl
import com.elementary.tasks.core.utils.TelephonyUtil
import com.github.naz013.common.Permissions
import com.github.naz013.common.intent.IntentKeys
import com.github.naz013.feature.common.livedata.observeEvent
import com.github.naz013.logging.Logger
import com.github.naz013.ui.common.compose.ComposeActivity
import com.github.naz013.ui.common.context.buildIntent
import com.github.naz013.ui.common.context.startActivity
import org.koin.androidx.compose.koinViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class BirthdayActionActivity : ComposeActivity() {
  private val viewModel by viewModel<BirthdayActionViewModel> { parametersOf(getId(), isTest()) }
  private val permissionFlowDelegate by lazy { PermissionFlowDelegateImpl(this) }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    initViewModel()
  }

  @Composable
  override fun ActivityContent() {
    val viewModel: BirthdayActionViewModel =
      koinViewModel {
        parametersOf(getId(), isTest())
      }
    BirthdayActionScreen(viewModel = viewModel)
  }

  private fun getId() = intent.getStringExtra(IntentKeys.INTENT_ID) ?: ""

  private fun isTest() = intent.getBooleanExtra(ARG_TEST, false)

  private fun initViewModel() {
    Logger.d(TAG, "initViewModel: ${getId()}")

    viewModel.redirectEvent.observeEvent(this) { event ->
      handleRedirect(event)
    }
    viewModel.showToast.observeEvent(this) {
      Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
    }
    lifecycle.addObserver(viewModel)
  }

  private fun handleRedirect(event: BirthdayActionViewModel.Redirect) {
    when (event) {
      is BirthdayActionViewModel.Redirect.Finish -> {
        finish()
      }

      is BirthdayActionViewModel.Redirect.MakeCall -> {
        permissionFlowDelegate.permissionFlow.askPermission(Permissions.CALL_PHONE) {
          TelephonyUtil.makeCall(event.phoneNumber, this)
          finish()
        }
      }

      is BirthdayActionViewModel.Redirect.SendSms -> {
        TelephonyUtil.sendSms(event.phoneNumber, this)
        finish()
      }
    }
  }

  companion object {
    private const val TAG = "BirthdayActionActivity"
    private const val ARG_TEST = "arg_test"

    fun mockTest(
      context: Context,
      id: String,
    ) {
      context.startActivity(BirthdayActionActivity::class.java) {
        putExtra(ARG_TEST, true)
        putExtra(IntentKeys.INTENT_ID, id)
      }
    }

    fun getLaunchIntent(
      context: Context,
      id: String,
    ): Intent =
      context.buildIntent(BirthdayActionActivity::class.java) {
        putExtra(IntentKeys.INTENT_ID, id)
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_MULTIPLE_TASK
      }
  }
}
