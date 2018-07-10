package com.elementary.tasks.core.services

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.SmsManager

import com.elementary.tasks.core.interfaces.SendListener

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

class SendReceiver(private val listener: SendListener?) : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        when (resultCode) {
            Activity.RESULT_OK -> listener?.messageSendResult(true)
            SmsManager.RESULT_ERROR_GENERIC_FAILURE -> listener?.messageSendResult(false)
            SmsManager.RESULT_ERROR_NO_SERVICE -> listener?.messageSendResult(false)
            SmsManager.RESULT_ERROR_NULL_PDU -> listener?.messageSendResult(false)
            SmsManager.RESULT_ERROR_RADIO_OFF -> listener?.messageSendResult(false)
        }
    }
}
