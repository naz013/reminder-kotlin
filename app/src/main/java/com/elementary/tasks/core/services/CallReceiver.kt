package com.elementary.tasks.core.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.PhoneStateListener
import android.telephony.TelephonyManager

import com.elementary.tasks.ReminderApp
import com.elementary.tasks.core.additional.FollowReminderActivity
import com.elementary.tasks.core.additional.QuickSmsActivity
import com.elementary.tasks.core.data.AppDb
import com.elementary.tasks.core.data.models.MissedCall
import com.elementary.tasks.core.utils.Constants
import com.elementary.tasks.core.utils.LogUtil
import com.elementary.tasks.core.utils.Prefs

import javax.inject.Inject

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

class CallReceiver : BroadcastReceiver() {

    private var mContext: Context? = null
    private var mIncomingNumber: String? = null
    private var prevState: Int = 0
    private var startCallTime: Long = 0

    @Inject
    var appDb: AppDb? = null

    init {
        ReminderApp.appComponent!!.inject(this)
    }

    override fun onReceive(context: Context, intent: Intent) {
        val telephony = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        if (telephony != null) {
            val customPhoneListener = CustomPhoneStateListener()
            telephony.listen(customPhoneListener, PhoneStateListener.LISTEN_CALL_STATE)
        }
        mContext = context
    }

    inner class CustomPhoneStateListener : PhoneStateListener() {

        override fun onCallStateChanged(state: Int, incomingNumber: String?) {
            val prefs = Prefs.getInstance(mContext)
            LogUtil.d(TAG, "onCallStateChanged: " + incomingNumber!!)
            if (incomingNumber != null && incomingNumber.length > 0) {
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
                        if (mIncomingNumber != null && isFollow) {
                            val number = mIncomingNumber
                            mContext!!.startActivity(Intent(mContext, FollowReminderActivity::class.java)
                                    .putExtra(Constants.SELECTED_CONTACT_NUMBER, number)
                                    .putExtra(Constants.SELECTED_TIME, startCallTime)
                                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP))
                            break
                        }
                    }
                    if (prevState == TelephonyManager.CALL_STATE_RINGING) {
                        prevState = state
                        val currTime = System.currentTimeMillis()
                        if (currTime - startCallTime >= 1000 * 10) {
                            LogUtil.d(TAG, "onCallStateChanged: is missed " + mIncomingNumber!!)
                            if (prefs.isMissedReminderEnabled && mIncomingNumber != null) {
                                val number = mIncomingNumber
                                var missedCall = appDb!!.missedCallsDao().getByNumber(number)
                                if (missedCall != null) {
                                    EventJobService.cancelMissedCall(missedCall.number)
                                } else {
                                    missedCall = MissedCall()
                                }
                                missedCall.dateTime = currTime
                                missedCall.number = number
                                appDb!!.missedCallsDao().insert(missedCall)
                                EventJobService.enableMissedCall(mContext, missedCall.number)
                                break
                            }
                        } else {
                            LogUtil.d(TAG, "onCallStateChanged: is quickSms " + mIncomingNumber!!)
                            if (mIncomingNumber != null && prefs.isQuickSmsEnabled) {
                                val number = mIncomingNumber
                                if (appDb!!.smsTemplatesDao().all.size > 0) {
                                    mContext!!.startActivity(Intent(mContext, QuickSmsActivity::class.java)
                                            .putExtra(Constants.SELECTED_CONTACT_NUMBER, number)
                                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                                }
                                break
                            }
                        }
                    }
                }
            }
        }
    }

    companion object {

        private val TAG = "CallReceiver"
    }
}