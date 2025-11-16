package com.elementary.tasks.reminder.dialog

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.compose.runtime.Composable
import com.elementary.tasks.core.data.Commands
import com.elementary.tasks.core.os.PermissionFlowDelegateImpl
import com.elementary.tasks.core.utils.TelephonyUtil
import com.github.naz013.common.Permissions
import com.github.naz013.common.intent.IntentKeys
import com.github.naz013.feature.common.livedata.observeEvent
import com.github.naz013.logging.Logger
import com.github.naz013.navigation.ActivityDestination
import com.github.naz013.navigation.DestinationScreen
import com.github.naz013.navigation.Navigator
import com.github.naz013.ui.common.compose.ComposeActivity
import com.github.naz013.ui.common.compose.foundation.DynamicScreen
import com.github.naz013.ui.common.context.buildIntent
import com.github.naz013.ui.common.context.startActivity
import org.koin.android.ext.android.inject
import org.koin.androidx.compose.koinViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class ReminderActionActivity : ComposeActivity() {

  private val viewModel by viewModel<ReminderActionActivityViewModel> {
    parametersOf(getId(), isTest())
  }
  private val navigator by inject<Navigator>()
  private val permissionFlowDelegate by lazy { PermissionFlowDelegateImpl(this) }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    initViewModel()
  }

  @Composable
  override fun ActivityContent() {
    val viewModel: ReminderActionActivityViewModel = koinViewModel()

    DynamicScreen(
      mobilePortrait = {
        ReminderActionScreen(viewModel = viewModel)
      }
    )
  }

  private fun getId() = intent.getStringExtra(IntentKeys.INTENT_ID) ?: ""
  private fun isTest() = intent.getBooleanExtra(ARG_TEST, false)

  private fun initViewModel() {
    Logger.d(TAG, "initViewModel: ${getId()}")

    viewModel.resultEvent.observeEvent(this) { commands ->
      when (commands) {
        Commands.DELETED -> {
        }

        else -> {
        }
      }
    }
    viewModel.redirectEvent.observeEvent(this) { event ->
      handleRedirect(event)
    }
    viewModel.showToast.observeEvent(this) {
      Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
    }
    lifecycle.addObserver(viewModel)
  }

  private fun handleRedirect(event: ReminderActionActivityViewModel.Redirect) {
    when (event) {
      is ReminderActionActivityViewModel.Redirect.Edit -> {
        navigator.navigate(
          ActivityDestination(
            screen = DestinationScreen.ReminderCreate,
            extras = Bundle().apply {
              putString(IntentKeys.INTENT_ID, event.id)
            },
            flags = Intent.FLAG_ACTIVITY_NEW_TASK,
            isLoggedIn = true,
            action = Intent.ACTION_VIEW
          )
        )
        finish()
      }

      is ReminderActionActivityViewModel.Redirect.Finish -> {
        finish()
      }

      is ReminderActionActivityViewModel.Redirect.SendEmail -> {
        TelephonyUtil.sendMail(
          context = this,
          email = event.email,
          subject = event.subject,
          message = event.message,
          filePath = event.filePath
        )
        finish()
      }

      is ReminderActionActivityViewModel.Redirect.OpenApp -> {
        TelephonyUtil.openApp(event.target, this)
        finish()
      }

      is ReminderActionActivityViewModel.Redirect.OpenLink -> {
        TelephonyUtil.openLink(event.target, this)
        finish()
      }

      is ReminderActionActivityViewModel.Redirect.SendSms -> {
        TelephonyUtil.sendSms(this, event.target, event.message)
      }

      is ReminderActionActivityViewModel.Redirect.MakeCall -> {
        permissionFlowDelegate.permissionFlow.askPermission(Permissions.CALL_PHONE) {
          TelephonyUtil.makeCall(event.target, this)
          finish()
        }
      }
    }
  }

//  private fun showFile() {
//    val reminder = mReminder ?: return
//    val path = reminder.attachmentFile
//    val intent = Intent(Intent.ACTION_VIEW)
//    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
//    try {
//      val uri = FileProvider.getUriForFile(
//        /* context = */ this,
//        /* authority = */ BuildConfig.APPLICATION_ID + ".provider",
//        /* file = */ File(path)
//      )
//      intent.data = uri
//      intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
//      startActivity(intent)
//    } catch (e: Exception) {
//      Toast.makeText(this, R.string.cant_find_app_for_that_file_type, Toast.LENGTH_LONG).show()
//    }
//  }
//
//  private fun showFavouriteNotification(text: String, notificationId: Int) {
//    val builder = NotificationCompat.Builder(this, Notifier.CHANNEL_REMINDER)
//    builder.setContentTitle(text)
//    val appName: String = if (BuildParams.isPro) {
//      getString(R.string.app_name_pro)
//    } else {
//      getString(R.string.app_name)
//    }
//    builder.setContentText(appName)
//    builder.setSmallIcon(R.drawable.ic_fluent_alert)
//    builder.color = colorOf(R.color.secondaryBlue)
//    val isWear = prefs.isWearEnabled
//    if (isWear) {
//      builder.setOnlyAlertOnce(true)
//      builder.setGroup("GROUP")
//      builder.setGroupSummary(true)
//    }
//    notifier.notify(notificationId, builder.build())
//    if (isWear) {
//      showWearNotification(
//        text,
//        appName,
//        notificationId
//      )
//    }
//  }
//
//  private fun showWearNotification(
//    text: String,
//    secondaryText: String,
//    notificationId: Int
//  ) {
//    Logger.d("showWearNotification: $secondaryText")
//    val wearableNotificationBuilder = NotificationCompat.Builder(this, Notifier.CHANNEL_REMINDER)
//    wearableNotificationBuilder.setSmallIcon(R.drawable.ic_fluent_alert)
//    wearableNotificationBuilder.setContentTitle(text)
//    wearableNotificationBuilder.setContentText(secondaryText)
//    wearableNotificationBuilder.color = colorOf(R.color.secondaryBlue)
//    wearableNotificationBuilder.setOngoing(false)
//    wearableNotificationBuilder.setOnlyAlertOnce(true)
//    wearableNotificationBuilder.setGroup(groupName)
//    wearableNotificationBuilder.setGroupSummary(false)
//    notifier.notify(notificationId, wearableNotificationBuilder.build())
//  }

  companion object {

    private const val TAG = "ReminderActionActivity"
    private const val ARG_TEST = "arg_test"

    fun mockTest(context: Context, id: String) {
      context.startActivity(ReminderActionActivity::class.java) {
        putExtra(ARG_TEST, true)
        putExtra(IntentKeys.INTENT_ID, id)
      }
    }

    fun getLaunchIntent(context: Context, id: String): Intent {
      return context.buildIntent(ReminderActionActivity::class.java) {
        putExtra(IntentKeys.INTENT_ID, id)
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_MULTIPLE_TASK
      }
    }
  }
}
