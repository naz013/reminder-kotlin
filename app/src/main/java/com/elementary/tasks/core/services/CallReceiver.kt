package com.elementary.tasks.core.services

import android.content.Context
import android.content.Intent
import android.telephony.PhoneStateListener
import android.telephony.TelephonyManager
import com.elementary.tasks.core.additional.FollowReminderActivity
import com.elementary.tasks.core.additional.QuickSmsActivity
import com.elementary.tasks.core.data.AppDb
import com.elementary.tasks.core.data.models.MissedCall
import com.elementary.tasks.core.os.SystemServiceProvider
import org.koin.core.component.inject
import timber.log.Timber

class CallReceiver : BaseBroadcast() {

  private val appDb by inject<AppDb>()
  private val jobScheduler by inject<JobScheduler>()
  private val systemServiceProvider by inject<SystemServiceProvider>()

  private var mIncomingNumber: String? = null
  private var prevState: Int = 0
  private var startCallTime: Long = 0

  override fun onReceive(context: Context, intent: Intent) {
    if ((intent.action ?: "") == "android.intent.action.PHONE_STATE") {
      val telephony = systemServiceProvider.provideTelephonyManager()
      if (telephony != null && prefs.isTelephonyAllowed) {
        val customPhoneListener = CustomPhoneStateListener(context, jobScheduler)
        telephony.listen(customPhoneListener, PhoneStateListener.LISTEN_CALL_STATE)
      }
    }
  }

  inner class CustomPhoneStateListener(
    private val context: Context,
    private val jobScheduler: JobScheduler
  ) : PhoneStateListener() {

    override fun onCallStateChanged(state: Int, incomingNumber: String?) {
      Timber.d("onCallStateChanged: $incomingNumber")
      if (!incomingNumber.isNullOrEmpty()) {
        mIncomingNumber = incomingNumber
      } else {
        return
      }
      when (state) {
        TelephonyManager.CALL_STATE_RINGING -> {
          prevState = state
          startCallTime = System.currentTimeMillis()
        }

        TelephonyManager.CALL_STATE_OFFHOOK -> prevState = state
        TelephonyManager.CALL_STATE_IDLE -> {
          if (prevState == TelephonyManager.CALL_STATE_OFFHOOK) {
            prevState = state
            val isFollow = prefs.isFollowReminderEnabled
            if (prefs.isTelephonyAllowed && mIncomingNumber != null && isFollow) {
              val number = mIncomingNumber
              if (number != null) {
                FollowReminderActivity.mockScreen(context, number, startCallTime)
              }
            }
          } else if (prevState == TelephonyManager.CALL_STATE_RINGING) {
            prevState = state
            val currTime = System.currentTimeMillis()
            if (currTime - startCallTime >= 1000 * 10) {
              val number = mIncomingNumber
              Timber.d("onCallStateChanged: is missed $number")
              if (prefs.isTelephonyAllowed && prefs.isMissedReminderEnabled && number != null) {
                var missedCall = appDb.missedCallsDao().getByNumber(number)
                if (missedCall != null) {
                  jobScheduler.cancelMissedCall(missedCall.number)
                } else {
                  missedCall = MissedCall()
                }
                missedCall.dateTime = currTime
                missedCall.number = number
                appDb.missedCallsDao().insert(missedCall)
                jobScheduler.scheduleMissedCall(missedCall.number)
              }
            } else {
              Timber.d("onCallStateChanged: is quickSms $mIncomingNumber")
              if (mIncomingNumber != null && prefs.isQuickSmsEnabled) {
                val number = mIncomingNumber
                if (prefs.isTelephonyAllowed && number != null && appDb.smsTemplatesDao().all()
                    .isNotEmpty()
                ) {
                  QuickSmsActivity.openScreen(context, number)
                }
              }
            }
          }
        }
      }
    }
  }
}
