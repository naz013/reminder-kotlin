package com.elementary.tasks.core

import android.app.ProgressDialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.media.RingtoneManager
import android.net.Uri
import android.os.Bundle
import android.os.PowerManager
import android.speech.tts.TextToSpeech
import android.text.TextUtils
import android.util.DisplayMetrics
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.elementary.tasks.R
import com.elementary.tasks.core.utils.*
import com.elementary.tasks.core.views.TextDrawable
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.io.File
import java.util.*
import java.util.concurrent.atomic.AtomicInteger

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
abstract class BaseNotificationActivity : ThemedActivity() {

    private var tts: TextToSpeech? = null
    private var mSendDialog: ProgressDialog? = null

    private var mTextToSpeechListener: TextToSpeech.OnInitListener = TextToSpeech.OnInitListener { status ->
        LogUtil.d(TAG, "onInit: ")
        if (status == TextToSpeech.SUCCESS && tts != null) {
            val result = tts!!.setLanguage(ttsLocale)
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                LogUtil.d(TAG, "This Language is not supported")
            } else {
                if (!TextUtils.isEmpty(summary)) {
                    try {
                        Thread.sleep(1000)
                    } catch (e: InterruptedException) {
                        e.printStackTrace()
                    }

                    if (Module.isLollipop) {
                        tts!!.speak(summary, TextToSpeech.QUEUE_FLUSH, null, null)
                    } else {
                        tts!!.speak(summary, TextToSpeech.QUEUE_FLUSH, null)
                    }
                }
            }
        } else {
            LogUtil.d(TAG, "Initialization Failed!")
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
        get() = SoundStackHolder.getInstance().sound

    protected open val isBirthdayInfiniteVibration: Boolean
        get() = true

    protected open val isBirthdayInfiniteSound: Boolean
        get() = true

    protected open val ttsLocale: Locale?
        get() {
            LogUtil.d(TAG, "getTtsLocale: ")
            return Language().getLocale(this, false)
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

    protected abstract fun call()

    protected abstract fun delay()

    protected abstract fun cancel()

    protected abstract fun favourite()

    protected abstract fun ok()

    protected abstract fun showSendingError()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        SoundStackHolder.getInstance().init(this)
        val current = instanceCount.incrementAndGet()
        LogUtil.d(TAG, "onCreate: " + current + ", " + TimeUtil.getFullDateTime(System.currentTimeMillis(), true, true))
    }

    protected fun init() {
        setUpScreenOptions()
        SoundStackHolder.getInstance().setMaxVolume(maxVolume)
    }

    override fun onDestroy() {
        super.onDestroy()
        val left = instanceCount.decrementAndGet()
        LogUtil.d(TAG, "onDestroy: $left")
    }

    override fun onPause() {
        super.onPause()
        SoundStackHolder.getInstance().cancelIncreaseSound()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (MotionEvent.ACTION_DOWN == event.action) {
            discardMedia()
        }
        return super.onTouchEvent(event)
    }

    protected fun setTextDrawable(button: FloatingActionButton, text: String) {
        val drawable = TextDrawable.builder()
                .beginConfig()
                .textColor(Color.BLACK)
                .useFont(Typeface.MONOSPACE)
                .fontSize(30)
                .bold()
                .toUpperCase()
                .endConfig()
                .buildRound(text, Color.TRANSPARENT)
        button.setImageDrawable(drawable)
    }

    protected fun colorify(vararg fab: FloatingActionButton) {
        for (button in fab) {
            button.backgroundTintList = ViewUtils.getFabState(this, themeUtil.colorAccent(), themeUtil.colorAccent())
        }
    }

    protected fun loadImage(imageView: ImageView) {
        imageView.visibility = View.GONE
        val imagePrefs = prefs.reminderImage
        val blur = prefs.isBlurEnabled
        LogUtil.d(TAG, "loadImage: $imagePrefs, blur $blur")
        val metrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(metrics)
        val width = metrics.widthPixels
        val height = (metrics.heightPixels * 0.75).toInt()
        when {
            imagePrefs.matches(Constants.DEFAULT.toRegex()) -> {
                if (blur && Module.isPro) {
                    Glide.with(this)
                            .load(R.drawable.photo)
                            .apply(RequestOptions.bitmapTransform(BlurTransformation(15, 2)))
                            .apply(RequestOptions.overrideOf(width, height))
                            .into(imageView)
                } else {
                    Glide.with(this)
                            .load(R.drawable.photo)
                            .apply(RequestOptions.overrideOf(width, height))
                            .into(imageView)
                }
                imageView.visibility = View.VISIBLE
            }
            imagePrefs.matches(Constants.NONE.toRegex()) -> imageView.visibility = View.GONE
            else -> {
                if (blur && Module.isPro) {
                    Glide.with(this)
                            .load(Uri.parse(imagePrefs))
                            .apply(RequestOptions.bitmapTransform(BlurTransformation(15, 2)))
                            .apply(RequestOptions.overrideOf(width, height))
                            .into(imageView)
                } else {
                    Glide.with(this)
                            .load(Uri.parse(imagePrefs))
                            .apply(RequestOptions.overrideOf(width, height))
                            .into(imageView)
                }
                imageView.visibility = View.VISIBLE
            }
        }
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
            screenLock.acquire()
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
        if (mSendDialog != null && mSendDialog!!.isShowing) {
            mSendDialog?.dismiss()
        }
    }

    protected fun discardNotification(id: Int) {
        discardMedia()
        val mNotifyMgr = NotificationManagerCompat.from(this)
        mNotifyMgr.cancel(id)
    }

    protected fun discardMedia() {
        if (sound != null) sound!!.stop(true)
    }

    protected fun showWearNotification(secondaryText: String) {
        LogUtil.d(TAG, "showWearNotification: ")
        if (Module.isJellyMR2) {
            val wearableNotificationBuilder = NotificationCompat.Builder(this, Notifier.CHANNEL_REMINDER)
            wearableNotificationBuilder.setSmallIcon(R.drawable.ic_notification_nv_white)
            wearableNotificationBuilder.setContentTitle(summary)
            wearableNotificationBuilder.setContentText(secondaryText)
            if (Module.isLollipop) {
                wearableNotificationBuilder.color = ViewUtils.getColor(this, R.color.bluePrimary)
            }
            wearableNotificationBuilder.setOngoing(false)
            wearableNotificationBuilder.setOnlyAlertOnce(true)
            wearableNotificationBuilder.setGroup("GROUP")
            wearableNotificationBuilder.setGroupSummary(false)
            val mNotifyMgr = NotificationManagerCompat.from(this)
            mNotifyMgr.notify(id, wearableNotificationBuilder.build())
        }
    }

    companion object {

        private const val TAG = "BNActivity"
        private const val MY_DATA_CHECK_CODE = 111

        private val instanceCount = AtomicInteger(0)
    }
}
