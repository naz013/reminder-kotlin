package com.elementary.tasks.core.utils

import android.app.Activity
import android.app.ActivityManager
import android.app.NotificationManager
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.media.AudioManager
import android.net.ConnectivityManager
import android.net.Uri
import android.provider.Settings
import android.speech.RecognizerIntent
import android.util.Base64
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.backdoor.engine.ObjectUtil
import com.elementary.tasks.R
import com.elementary.tasks.core.appWidgets.voiceControl.VoiceWidgetDialog
import com.elementary.tasks.core.contacts.ContactsActivity
import com.elementary.tasks.core.interfaces.LCAMListener
import com.elementary.tasks.core.services.GeolocationService
import com.elementary.tasks.reminder.create_edit.fragments.ReminderInterface
import com.elementary.tasks.voice.ConversationActivity
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import java.io.UnsupportedEncodingException
import java.util.*

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

object SuperUtil {

    private const val TAG = "SuperUtil"

    fun hasVolumePermission(context: Context): Boolean {
        if (Module.isNougat) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager?
            return notificationManager != null && notificationManager.isNotificationPolicyAccessGranted
        }
        return true
    }

    fun stopService(context: Context, clazz: Class<*>) {
        context.stopService(Intent(context, clazz))
    }

    fun startGpsTracking(context: Context) {
        if (SuperUtil.isServiceRunning(context, GeolocationService::class.java)) {
            return
        }
        val intent = Intent(context, GeolocationService::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        if (Module.isO) {
            context.startForegroundService(intent)
        } else {
            context.startService(intent)
        }
    }

    fun isHeadsetUsing(context: Context): Boolean {
        val manager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager?
        return manager != null && (manager.isBluetoothA2dpOn || manager.isWiredHeadsetOn)
    }

    fun getString(fragment: Fragment, id: Int): String {
        return if (fragment.isAdded) {
            fragment.getString(id)
        } else
            ""
    }

    fun isDoNotDisturbEnabled(context: Context): Boolean {
        if (!Module.isMarshmallow) {
            return false
        }
        val mNotificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        return if (mNotificationManager.currentInterruptionFilter == NotificationManager.INTERRUPTION_FILTER_ALARMS || mNotificationManager.currentInterruptionFilter == NotificationManager.INTERRUPTION_FILTER_NONE) {
            LogUtil.d(TAG, "isDoNotDisturbEnabled: true")
            true
        } else {
            LogUtil.d(TAG, "isDoNotDisturbEnabled: false")
            false
        }
    }

    fun checkNotificationPermission(activity: Context): Boolean {
        val notificationManager = activity.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        return !(Module.isMarshmallow && !notificationManager.isNotificationPolicyAccessGranted)
    }

    fun askNotificationPermission(activity: Activity) {
        if (Module.isMarshmallow) {
            val builder = Dialogues.getDialog(activity)
            builder.setMessage(R.string.for_correct_work_of_application)
            builder.setPositiveButton(R.string.grant) { dialog, which ->
                dialog.dismiss()
                val intent = Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)
                try {
                    activity.startActivity(intent)
                } catch (ignored: ActivityNotFoundException) {
                }
            }
            builder.setNegativeButton(R.string.cancel) { dialog, _ -> dialog.dismiss() }
            builder.create().show()
        }
    }

    fun checkLocationEnable(context: Context): Boolean {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val isGPSEnabled = locationManager
                .isProviderEnabled(LocationManager.GPS_PROVIDER)
        val isNetworkEnabled = locationManager
                .isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        return !(!isGPSEnabled && !isNetworkEnabled)
    }

    fun showLocationAlert(context: Context, callbacks: ReminderInterface) {
        callbacks.showSnackbar(context.getString(R.string.gps_not_enabled), context.getString(R.string.action_settings), View.OnClickListener {
            val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            context.startActivity(intent)
        })
    }

    fun getObjectPrint(o: Any, clazz: Class<*>): String {
        return ObjectUtil.getObjectPrint(o, clazz)
    }

    fun selectContact(activity: Activity, requestCode: Int) {
        activity.startActivityForResult(Intent(activity, ContactsActivity::class.java), requestCode)
    }

    fun getAddress(currentLat: Double, currentLong: Double): String {
        return String.format(Locale.getDefault(), "%.5f, %.5f", currentLat, currentLong)
    }

    fun isGooglePlayServicesAvailable(a: Activity): Boolean {
        val resultCode = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(a)
        return resultCode == ConnectionResult.SUCCESS
    }

    fun checkGooglePlayServicesAvailability(a: Activity): Boolean {
        val googleAPI = GoogleApiAvailability.getInstance()
        val result = googleAPI.isGooglePlayServicesAvailable(a)
        LogUtil.d(TAG, "Result is: $result")
        return if (result != ConnectionResult.SUCCESS) {
            if (googleAPI.isUserResolvableError(result)) {
                googleAPI.getErrorDialog(a, result, 69).show()
            }
            false
        } else {
            true
        }
    }

    fun isConnected(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val netInfo = cm.activeNetworkInfo
        return netInfo != null && netInfo.isConnectedOrConnecting
    }

    fun showLCAM(context: Context, listener: LCAMListener?, vararg actions: String) {
        val builder = Dialogues.getDialog(context)
        builder.setItems(actions) { dialog, item ->
            dialog.dismiss()
            listener?.onAction(item)
        }
        val alert = builder.create()
        alert.show()
    }

    fun isServiceRunning(context: Context, serviceClass: Class<*>): Boolean {
        val manager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        for (service in manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.name == service.service.className) {
                return true
            }
        }
        return false
    }

    fun appendString(vararg strings: String): String {
        val stringBuilder = StringBuilder()
        for (string in strings) {
            stringBuilder.append(string)
        }
        return stringBuilder.toString()
    }

    fun getAfterTime(timeString: String): Long {
        return if (timeString.length == 6 && !timeString.matches("000000".toRegex())) {
            val hours = timeString.substring(0, 2)
            val minutes = timeString.substring(2, 4)
            val seconds = timeString.substring(4, 6)
            val hour = Integer.parseInt(hours)
            val minute = Integer.parseInt(minutes)
            val sec = Integer.parseInt(seconds)
            val s: Long = 1000
            val m = s * 60
            val h = m * 60
            hour * h + minute * m + sec * s
        } else
            0
    }

    fun startVoiceRecognitionActivity(activity: Activity, requestCode: Int, isLive: Boolean) {
        val intent: Intent
        if (isLive) {
            intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            intent.putExtra(RecognizerIntent.EXTRA_PROMPT, activity.getString(R.string.say_something))
        } else if (Prefs.getInstance(activity).isLiveEnabled) {
            (activity as? VoiceWidgetDialog)?.finish()
            intent = Intent(activity, ConversationActivity::class.java)
        } else {
            intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Language.getLanguage(Prefs.getInstance(activity).voiceLocale))
            intent.putExtra(RecognizerIntent.EXTRA_PROMPT, activity.getString(R.string.say_something))
        }
        try {
            activity.startActivityForResult(intent, requestCode)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(activity, activity.getString(R.string.no_recognizer_found), Toast.LENGTH_SHORT).show()
        }

    }

    fun isAppInstalled(context: Context, packageName: String): Boolean {
        val pm = context.packageManager
        val installed: Boolean
        installed = try {
            pm.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }

        return installed
    }

    fun installSkype(context: Context) {
        val marketUri = Uri.parse("market://details?id=com.skype.raider")
        val intent = Intent(Intent.ACTION_VIEW, marketUri)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        try {
            context.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            e.printStackTrace()
        }

    }

    fun isSkypeClientInstalled(context: Context): Boolean {
        val myPackageMgr = context.packageManager
        try {
            myPackageMgr.getPackageInfo("com.skype.raider", PackageManager.GET_ACTIVITIES)
        } catch (e: PackageManager.NameNotFoundException) {
            return false
        }

        return true
    }

    fun decrypt(string: String): String {
        var result = ""
        val byte_string = Base64.decode(string, Base64.DEFAULT)
        try {
            result = String(byte_string, charset("UTF-8"))
        } catch (e1: UnsupportedEncodingException) {
            e1.printStackTrace()
        }
        return result
    }

    fun encrypt(string: String): String {
        var string_byted: ByteArray? = null
        try {
            string_byted = string.toByteArray(charset("UTF-8"))
        } catch (e: UnsupportedEncodingException) {
            e.printStackTrace()
        }
        return Base64.encodeToString(string_byted, Base64.DEFAULT).trim { it <= ' ' }
    }

    fun launchMarket(context: Context) {
        val uri = Uri.parse("market://details?id=" + context.packageName)
        val goToMarket = Intent(Intent.ACTION_VIEW, uri)
        try {
            context.startActivity(goToMarket)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(context, context.getString(R.string.could_not_launch_market), Toast.LENGTH_SHORT).show()
        }
    }
}
