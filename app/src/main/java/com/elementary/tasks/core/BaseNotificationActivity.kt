package com.elementary.tasks.core

import android.app.ProgressDialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.os.Bundle
import android.os.PowerManager
import android.speech.tts.TextToSpeech
import android.text.TextUtils
import android.view.MotionEvent
import android.view.WindowManager
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.elementary.tasks.R
import com.elementary.tasks.ReminderApp
import com.elementary.tasks.core.utils.*
import timber.log.Timber
import java.io.File
import java.util.*
import java.util.concurrent.atomic.AtomicInteger
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
@Suppress("DEPRECATION")
abstract class BaseNotificationActivity : ThemedActivity() {

    private var tts: TextToSpeech? = null
    private var mSendDialog: ProgressDialog? = null
    @Inject
    lateinit var soundStackHolder: SoundStackHolder

    private var mTextToSpeechListener: TextToSpeech.OnInitListener = TextToSpeech.OnInitListener { status ->
        if (status == TextToSpeech.SUCCESS && tts != null) {
            val result = tts?.setLanguage(ttsLocale)
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Timber.d("This Language is not supported")
            } else {
                if (!TextUtils.isEmpty(summary)) {
                    try {
                        Thread.sleep(1000)
                    } catch (e: InterruptedException) {
                        e.printStackTrace()
                    }

                    if (Module.isLollipop) {
                        tts?.speak(summary, TextToSpeech.QUEUE_FLUSH, null, null)
                    } else {
                        tts?.speak(summary, TextToSpeech.QUEUE_FLUSH, null)
                    }
                }
            }
        } else {
            Timber.d("Initialization Failed!")
        }
    }
    protected var mSendListener = { isSent: Boolean ->
        hideProgressDialog()
        if (isSent) {
            finish()
        } else {
            showSendingError()
        }
    }

    protected abstract val melody: String

    protected abstract val isScreenResumed: Boolean

    protected abstract val isVibrate: Boolean

    protected abstract val summary: String

    protected abstract val uuId: String

    protected abstract val id: Int

    protected abstract val ledColor: Int

    protected abstract val isAwakeDevice: Boolean

    protected abstract val isUnlockDevice: Boolean

    protected abstract val isGlobal: Boolean

    protected abstract val maxVolume: Int

    protected open val sound: Sound?
        get() = soundStackHolder.sound

    protected open val isBirthdayInfiniteVibration: Boolean
        get() = true

    protected open val isBirthdayInfiniteSound: Boolean
        get() = true

    protected open val ttsLocale: Locale?
        get() {
            return language.getLocale(false)
        }

    protected open val soundUri: Uri
        get() {
            if (!TextUtils.isEmpty(melody) && !Sound.isDefaultMelody(melody)) {
                return UriUtil.getUri(this, melody)
            } else {
                val defMelody = prefs.melodyFile
                if (!TextUtils.isEmpty(defMelody) && !Sound.isDefaultMelody(defMelody)) {
                    val sound = File(defMelody)
                    if (sound.exists()) {
                        return UriUtil.getUri(this, sound)
                    }
                }
            }
            return RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        }

    init {
        ReminderApp.appComponent.inject(this)
    }

    protected abstract fun showSendingError()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val current = instanceCount.incrementAndGet()
        Timber.d("onCreate: $current, ${TimeUtil.getFullDateTime(System.currentTimeMillis(), true)}")
    }

    protected fun init() {
        setUpScreenOptions()
        soundStackHolder.setMaxVolume(maxVolume)
    }

    override fun onDestroy() {
        super.onDestroy()
        val left = instanceCount.decrementAndGet()
        Timber.d("onDestroy: left screens -> $left")
    }

    override fun onPause() {
        super.onPause()
        soundStackHolder.cancelIncreaseSound()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (MotionEvent.ACTION_DOWN == event.action) {
            discardMedia()
        }
        return super.onTouchEvent(event)
    }

    private fun setUpScreenOptions() {
        var isFull = prefs.isDeviceUnlockEnabled
        var isWake = prefs.isDeviceAwakeEnabled
        if (!isGlobal) {
            isFull = isUnlockDevice
            isWake = isAwakeDevice
        }
        if (isFull) {
            runOnUiThread {
                window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                        or WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                        or WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD)
            }
        }
        if (isWake) {
            val screenLock = (getSystemService(Context.POWER_SERVICE) as PowerManager).newWakeLock(
                    PowerManager.SCREEN_BRIGHT_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP, "reminder:ReminderAPPTAG")
            screenLock.acquire(10*60*1000L /*10 minutes*/)
            screenLock.release()
        }
    }

    protected fun removeFlags() {
        window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                or WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                or WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                or WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD)

        if (tts != null) {
            tts?.stop()
            tts?.shutdown()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == MY_DATA_CHECK_CODE) {
            if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
                tts = TextToSpeech(this, mTextToSpeechListener)
            } else {
                val installTTSIntent = Intent()
                installTTSIntent.action = TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA
                try {
                    startActivity(installTTSIntent)
                } catch (e: ActivityNotFoundException) {
                    e.printStackTrace()
                }
            }
        }
    }

    protected fun startTts() {
        val checkTTSIntent = Intent()
        checkTTSIntent.action = TextToSpeech.Engine.ACTION_CHECK_TTS_DATA
        try {
            startActivityForResult(checkTTSIntent, MY_DATA_CHECK_CODE)
        } catch (e: ActivityNotFoundException) {
            e.printStackTrace()
        }
    }

    protected fun showProgressDialog(message: String) {
        hideProgressDialog()
        mSendDialog = ProgressDialog.show(this, null, message, true, false)
    }

    private fun hideProgressDialog() {
        if (mSendDialog != null && mSendDialog?.isShowing == true) {
            mSendDialog?.dismiss()
        }
    }

    protected fun discardNotification(id: Int) {
        Timber.d("discardNotification: $id")
        discardMedia()
        Notifier.getManager(this)?.cancel(id)
    }

    protected fun discardMedia() {
        sound?.stop(true)
    }

    protected fun showWearNotification(secondaryText: String) {
        Timber.d("showWearNotification: $secondaryText")
        val wearableNotificationBuilder = NotificationCompat.Builder(this, Notifier.CHANNEL_REMINDER)
        wearableNotificationBuilder.setSmallIcon(R.drawable.ic_notification_nv_white)
        wearableNotificationBuilder.setContentTitle(summary)
        wearableNotificationBuilder.setContentText(secondaryText)
        if (Module.isLollipop) {
            wearableNotificationBuilder.color = ContextCompat.getColor(this, R.color.bluePrimary)
        }
        wearableNotificationBuilder.setOngoing(false)
        wearableNotificationBuilder.setOnlyAlertOnce(true)
        wearableNotificationBuilder.setGroup("GROUP")
        wearableNotificationBuilder.setGroupSummary(false)
        Notifier.getManager(this)?.notify(id, wearableNotificationBuilder.build())
    }

    companion object {
        private const val MY_DATA_CHECK_CODE = 111

        private val instanceCount = AtomicInteger(0)
    }
}
