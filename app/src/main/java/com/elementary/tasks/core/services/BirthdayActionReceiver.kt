package com.elementary.tasks.core.services

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.elementary.tasks.Actions
import com.elementary.tasks.birthdays.preview.ShowBirthdayActivity
import com.elementary.tasks.core.data.AppDb
import com.elementary.tasks.core.data.models.Birthday
import com.elementary.tasks.core.utils.Constants
import com.elementary.tasks.core.utils.Module
import com.elementary.tasks.core.utils.Permissions
import com.elementary.tasks.core.utils.SuperUtil
import com.elementary.tasks.core.utils.TelephonyUtil
import com.elementary.tasks.core.utils.datetime.DateTimeManager
import com.elementary.tasks.core.utils.launchDefault
import org.koin.core.component.inject
import timber.log.Timber
import java.util.Calendar

class BirthdayActionReceiver : BaseBroadcast() {

  private val appDb by inject<AppDb>()

  override fun onReceive(context: Context, intent: Intent?) {
    if (intent != null) {
      val action = intent.action
      Timber.d("onReceive: $action")
      if (action != null) {
        when {
          action.matches(ACTION_CALL.toRegex()) -> makeCall(context, intent)
          action.matches(ACTION_SMS.toRegex()) -> sendSms(context, intent)
          action.matches(PermanentBirthdayReceiver.ACTION_HIDE.toRegex()) -> {
            hidePermanent(intent.getStringExtra(Constants.INTENT_ID) ?: "")
          }
          else -> showReminder(context, intent)
        }
      }
    }
  }

  private fun updateBirthday(item: Birthday) {
    val calendar = Calendar.getInstance()
    calendar.timeInMillis = System.currentTimeMillis()
    val year = calendar.get(Calendar.YEAR)
    item.showedYear = year
    item.updatedAt = DateTimeManager.gmtDateTime
    launchDefault {
      appDb.birthdaysDao().insert(item)
    }
  }

  private fun sendSms(context: Context, intent: Intent) {
    val item = appDb.birthdaysDao().getById(intent.getStringExtra(Constants.INTENT_ID)
      ?: "")
    if (item != null) {
      TelephonyUtil.sendSms(item.number, context)
      updateBirthday(item)
      finish(item.uniqueId)
    } else {
      hidePermanent(intent.getStringExtra(Constants.INTENT_ID) ?: "")
    }
  }

  private fun makeCall(context: Context, intent: Intent) {
    val item = appDb.birthdaysDao().getById(intent.getStringExtra(Constants.INTENT_ID)
      ?: "")
    if (item != null && Permissions.checkPermission(context, Permissions.CALL_PHONE)) {
      TelephonyUtil.makeCall(item.number, context)
      updateBirthday(item)
      finish(item.uniqueId)
    } else {
      hidePermanent(intent.getStringExtra(Constants.INTENT_ID) ?: "")
    }
  }

  private fun showReminder(context: Context, intent: Intent) {
    val birthday = appDb.birthdaysDao().getById(intent.getStringExtra(Constants.INTENT_ID)
      ?: "") ?: return
    sendCloseBroadcast(context, birthday.uuId)
    if (Module.is10 || SuperUtil.isPhoneCallActive(context)) {
      qAction(birthday, context)
    } else {
      val notificationIntent = ShowBirthdayActivity.getLaunchIntent(context, birthday.uuId)
      notificationIntent.putExtra(Constants.INTENT_NOTIFICATION, true)
      context.startActivity(notificationIntent)
      notifier.cancel(PermanentBirthdayReceiver.BIRTHDAY_PERM_ID)
    }
  }

  private fun qAction(birthday: Birthday, context: Context) {
    sendCloseBroadcast(context, birthday.uuId)
    ContextCompat.startForegroundService(context,
      EventOperationalService.getIntent(context, birthday.uuId,
        EventOperationalService.TYPE_BIRTHDAY,
        EventOperationalService.ACTION_PLAY,
        birthday.uniqueId))
  }

  private fun hidePermanent(id: String) {
    if (id.isEmpty()) return
    val item = appDb.birthdaysDao().getById(id)
    if (item != null) {
      updateBirthday(item)
      finish(item.uniqueId)
    }
  }

  private fun sendCloseBroadcast(context: Context, id: String) {
    val intent = Intent(ShowBirthdayActivity.ACTION_STOP_BG_ACTIVITY)
    intent.putExtra(Constants.INTENT_ID, id)
    LocalBroadcastManager.getInstance(context).sendBroadcast(intent)
  }

  private fun finish(id: Int) {
    notifier.cancel(id)
    updatesHelper.updateWidgets()
    updatesHelper.updateCalendarWidget()
  }

  companion object {
    private const val ACTION_SHOW = Actions.Birthday.ACTION_SHOW_FULL
    private const val ACTION_CALL = Actions.Birthday.ACTION_CALL
    private const val ACTION_SMS = Actions.Birthday.ACTION_SMS

    fun hide(context: Context, id: String): Intent {
      return intent(context, id, PermanentBirthdayReceiver.ACTION_HIDE)
    }

    fun call(context: Context, id: String): Intent {
      return intent(context, id, ACTION_CALL)
    }

    fun show(context: Context, id: String): Intent {
      return intent(context, id, ACTION_SHOW)
    }

    fun sms(context: Context, id: String): Intent {
      return intent(context, id, ACTION_SMS)
    }

    private fun intent(context: Context, id: String, action: String): Intent {
      val intent = Intent(context, BirthdayActionReceiver::class.java)
      intent.action = action
      intent.putExtra(Constants.INTENT_ID, id)
      return intent
    }
  }
}
