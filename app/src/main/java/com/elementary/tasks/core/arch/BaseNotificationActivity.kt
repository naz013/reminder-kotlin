package com.elementary.tasks.core.arch

import android.content.ActivityNotFoundException
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.os.Bundle
import android.os.PowerManager
import android.speech.tts.TextToSpeech
import android.text.TextUtils
import android.view.MotionEvent
import androidx.annotation.LayoutRes
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.databinding.ViewDataBinding
import com.elementary.tasks.R
import com.elementary.tasks.core.utils.*
import org.koin.android.ext.android.inject
import timber.log.Timber
import java.io.IOException
import java.util.*
import java.util.concurrent.atomic.AtomicInteger

abstract class BaseNotificationActivity<B : ViewDataBinding>(@LayoutRes layoutRes: Int) : BindingActivity<B>(layoutRes) {

    private var tts: TextToSpeech? = null
    private var mWakeLock: PowerManager.WakeLock? = null
    private val soundStackHolder: SoundStackHolder by inject()

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

                    tts?.speak(summary, TextToSpeech.QUEUE_FLUSH, null, null)
                }
            }
        } else {
            Timber.d("Initialization Failed!")
        }
    }

    protected abstract val melody: String

    protected abstract val isScreenResumed: Boolean

    protected abstract val isVibrate: Boolean

    protected abstract val summary: String

    protected abstract val uuId: String

    protected abstract val id: Int

    protected abstract val ledColor: Int

    protected abstract val isUnlockDevice: Boolean

    protected abstract val isGlobal: Boolean

    protected abstract val maxVolume: Int

    protected abstract val priority: Int

    protected abstract val groupName: String

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
        get() = ReminderUtils.getSoundUri(this, prefs, melody)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val current = instanceCount.incrementAndGet()
        Timber.d("onCreate: $current, ${TimeUtil.getFullDateTime(System.currentTimeMillis(), true)}")
    }

    protected fun init() {
        setUpScreenOptions()
        soundStackHolder.initParams()
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

    private fun canUnlockScreen(): Boolean {
        return if (isGlobal) {
            if (prefs.isDeviceUnlockEnabled) {
                priority >= prefs.unlockPriority
            } else {
                false
            }
        } else {
            isUnlockDevice
        }
    }

    private fun setUpScreenOptions() {
        Timber.d("setUpScreenOptions: ${canUnlockScreen()}")
        if (canUnlockScreen()) {
            SuperUtil.turnScreenOn(this, window)
            SuperUtil.unlockOn(this, window)
        }
        mWakeLock = SuperUtil.wakeDevice(this)
    }

    protected fun removeFlags() {
        if (tts != null) {
            tts?.stop()
            tts?.shutdown()
        }
        if (canUnlockScreen()) {
            SuperUtil.unlockOff(this, window)
            SuperUtil.turnScreenOff(this, window, mWakeLock)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
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
        wearableNotificationBuilder.setSmallIcon(R.drawable.ic_twotone_notifications_white)
        wearableNotificationBuilder.setContentTitle(summary)
        wearableNotificationBuilder.setContentText(secondaryText)
        wearableNotificationBuilder.color = ContextCompat.getColor(this, R.color.bluePrimary)
        wearableNotificationBuilder.setOngoing(false)
        wearableNotificationBuilder.setOnlyAlertOnce(true)
        wearableNotificationBuilder.setGroup(groupName)
        wearableNotificationBuilder.setGroupSummary(false)
        Notifier.getManager(this)?.notify(id, wearableNotificationBuilder.build())
    }

    protected fun playDefaultMelody() {
        if (sound == null) return
        Timber.d("playDefaultMelody: ")
        try {
            val afd = assets.openFd("sounds/beep.mp3")
            sound?.playAlarm(afd)
        } catch (e: IOException) {
            e.printStackTrace()
            sound?.playAlarm(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION), false, prefs.playbackDuration)
        }
    }

    companion object {
        private const val MY_DATA_CHECK_CODE = 111

        private val instanceCount = AtomicInteger(0)
    }
}
