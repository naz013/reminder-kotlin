package com.elementary.tasks.core.services

import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.IBinder
import android.speech.tts.TextToSpeech
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.elementary.tasks.Actions
import com.elementary.tasks.R
import com.elementary.tasks.core.data.AppDb
import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.core.utils.*
import com.elementary.tasks.reminder.preview.ReminderDialogQActivity
import org.koin.android.ext.android.inject
import timber.log.Timber
import java.io.IOException
import java.util.*
import java.util.concurrent.atomic.AtomicInteger

class EventOperationalService : Service(), Sound.PlaybackCallback {

    private val notifier: Notifier by inject()
    private val appDb: AppDb by inject()
    private val prefs: Prefs by inject()
    private val language: Language by inject()
    private val soundStackHolder: SoundStackHolder by inject()

    private val ttsLocale: Locale? = language.getLocale(false)

    private var tts: TextToSpeech? = null
    private var mTextToSpeechListener: TextToSpeech.OnInitListener = TextToSpeech.OnInitListener { status ->
        if (status == TextToSpeech.SUCCESS) {
            val result = tts?.setLanguage(ttsLocale)
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Timber.d("TTS This Language is not supported, $ttsLocale")
            } else {
                Timber.d("TTS initialized!")
            }
        } else {
            Timber.d("TTS Initialization Failed!")
        }
    }

    override fun onCreate() {
        super.onCreate()
        showForegroundNotification()
        tts = TextToSpeech(applicationContext, mTextToSpeechListener)
        soundStackHolder.playbackCallback = this
        soundStackHolder.initParams()
    }

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    private fun showForegroundNotification() {
        val builder = NotificationCompat.Builder(applicationContext, Notifier.CHANNEL_SYSTEM)
        builder.color = ThemeUtil.getSecondaryColor(applicationContext)
        builder.setSmallIcon(R.drawable.ic_twotone_music_note_24px)
        builder.setContentTitle(getString(R.string.reminder_ongoing_service))
        builder.setContentText(getString(R.string.app_title))
        startForeground(3214, builder.build())
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        onHandleIntent(intent)
        return super.onStartCommand(intent, flags, startId)
    }

    private fun onHandleIntent(intent: Intent?) {
        if (intent != null) {
            val id = intent.getStringExtra(ARG_ID) ?: ""
            val type = intent.getStringExtra(ARG_TYPE) ?: ""

            if (id.isEmpty() && type.isEmpty()) {
                checkForStop()
                return
            }

            if (type.isNotEmpty()) {
                when (type) {
                    TYPE_REMINDER -> {
                        val reminder = appDb.reminderDao().getById(id) ?: return
                        when (ACTION_PLAY) {
                            intent.action -> {
                                showReminderNotification(reminder)
                            }
                        }
                    }
                    TYPE_BIRTHDAY -> {
                        when {
                            ACTION_PLAY == intent.action -> {

                            }
                            ACTION_STOP == intent.action -> {

                            }
                        }
                    }
                    TYPE_MESSED -> {
                        when {
                            ACTION_PLAY == intent.action -> {

                            }
                            ACTION_STOP == intent.action -> {

                            }
                        }
                    }
                }
            }
            when {
                ACTION_PLAY == intent.action -> {
                    val current = instanceCount.incrementAndGet()
                    Timber.d("PLAY: $current, ${TimeUtil.getFullDateTime(System.currentTimeMillis(), true)}")
                }
                ACTION_STOP == intent.action -> {
                    val left = instanceCount.decrementAndGet()
                    Timber.d("STOP: left screens -> $left")
                    soundStackHolder.sound?.stop(true)
                    checkForStop()
                }
                else -> checkForStop()
            }
        }
    }

    private fun showReminderNotification(reminder: Reminder) {
        Timber.d("showReminderNotification: $reminder")

        var isTtsEnabled = prefs.isTtsEnabled
        if (!reminder.useGlobal) {
            isTtsEnabled = reminder.notifyByVoice
        }

        val builder = NotificationCompat.Builder(this, Notifier.CHANNEL_REMINDER)
        if (isTtsEnabled) {
            if ((!SuperUtil.isDoNotDisturbEnabled(this) ||
                            (SuperUtil.checkNotificationPermission(this) && prefs.isSoundInSilentModeEnabled))) {
                playTts(reminder.summary, reminder.melodyPath)
            }
        } else {
            if ((!SuperUtil.isDoNotDisturbEnabled(this) ||
                            (SuperUtil.checkNotificationPermission(this) && prefs.isSoundInSilentModeEnabled))) {
                val melody = ReminderUtils.getSound(applicationContext, prefs, reminder.melodyPath)
                if (melody.melodyType == ReminderUtils.MelodyType.FILE) {
                    playMelody(reminder.melodyPath)
                } else {
                    applicationContext.grantUriPermission("com.android.systemui", melody.uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    builder.setSound(melody.uri, prefs.soundStream)
                }
            }
        }

        if (prefs.isVibrateEnabled) {
            val pattern: LongArray = if (prefs.isInfiniteVibrateEnabled) {
                longArrayOf(150, 86400000)
            } else {
                longArrayOf(150, 400, 100, 450, 200, 500, 300, 500)
            }
            builder.setVibrate(pattern)
        }
        builder.priority = priority(reminder.priority)
        builder.setContentTitle(reminder.summary)
        builder.setAutoCancel(false)
        builder.priority = NotificationCompat.PRIORITY_MAX
        if (prefs.isManualRemoveEnabled) {
            builder.setOngoing(false)
        } else {
            builder.setOngoing(true)
        }
        if (Module.isPro && prefs.isLedEnabled) {
            builder.setLights(ledColor(reminder.color), 500, 1000)
        }
        builder.setContentText(appName())
        builder.setSmallIcon(R.drawable.ic_twotone_notifications_white)
        builder.color = ThemeUtil.getSecondaryColor(applicationContext)
        builder.setCategory(NotificationCompat.CATEGORY_REMINDER)

        if (reminder.priority > 2) {
            val fullScreenIntent = ReminderDialogQActivity.getLaunchIntent(applicationContext, reminder.uuId)
            val fullScreenPendingIntent = PendingIntent.getActivity(this, reminder.uniqueId, fullScreenIntent, PendingIntent.FLAG_CANCEL_CURRENT)
            builder.setFullScreenIntent(fullScreenPendingIntent, true)
        } else {
            val notificationIntent = Intent(this, ReminderActionReceiver::class.java)
            notificationIntent.action = ReminderActionReceiver.ACTION_SHOW
            notificationIntent.putExtra(Constants.INTENT_ID, reminder.uuId)
            val intent = PendingIntent.getBroadcast(this, reminder.uniqueId, notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT)
            builder.setContentIntent(intent)
        }
        val dismissIntent = Intent(applicationContext, ReminderActionReceiver::class.java)
        dismissIntent.action = ReminderActionReceiver.ACTION_HIDE
        dismissIntent.putExtra(Constants.INTENT_ID, reminder.uuId)
        val piDismiss = PendingIntent.getBroadcast(applicationContext, reminder.uniqueId, dismissIntent, PendingIntent.FLAG_CANCEL_CURRENT)
        builder.addAction(R.drawable.ic_twotone_done_white, applicationContext.getString(R.string.ok), piDismiss)

        val isWear = prefs.isWearEnabled
        if (isWear) {
            builder.setOnlyAlertOnce(true)
            builder.setGroup("reminder")
            builder.setGroupSummary(true)
        }
        Notifier.getManager(this)?.notify(reminder.uniqueId, builder.build())
        if (isWear) {
            showWearNotification(reminder.uniqueId, reminder.summary, appName(), "reminder")
        }
    }

    private fun appName(): String {
        return if (Module.isPro) {
            getString(R.string.app_name_pro)
        } else {
            getString(R.string.app_name)
        }
    }

    private fun ledColor(color: Int): Int {
        return if (Module.isPro) {
            if (color != -1) {
                LED.getLED(color)
            } else {
                LED.getLED(prefs.ledColor)
            }
        } else {
            LED.getLED(0)
        }
    }

    private fun priority(priority: Int): Int {
        return when (priority) {
            0 -> NotificationCompat.PRIORITY_MIN
            1 -> NotificationCompat.PRIORITY_LOW
            2 -> NotificationCompat.PRIORITY_DEFAULT
            3 -> NotificationCompat.PRIORITY_HIGH
            else -> NotificationCompat.PRIORITY_MAX
        }
    }

    private fun checkForStop() {
        if (instanceCount.get() <= 0) {
            stopForeground(true)
        }
    }

    override fun onStart() {
        Timber.d("onStart: ")
    }

    override fun onFinish() {
        Timber.d("onFinish: ")
        val left = instanceCount.decrementAndGet()
        Timber.d("onFinish: left screens -> $left")
        checkForStop()
    }

    override fun onDestroy() {
        tts?.stop()
        tts?.shutdown()
        super.onDestroy()
        soundStackHolder.playbackCallback = null
    }

    private fun showWearNotification(id: Int, summary: String, secondaryText: String, groupName: String) {
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

    private fun playTts(summary: String, melodyPath: String) {
        try {
            playDefaultMelody()
            tts?.speak(summary, TextToSpeech.QUEUE_FLUSH, null, null)
        } catch (e: Exception) {
            playMelody(melodyPath)
        }
    }

    private fun playMelody(melodyPath: String) {
        val soundUri = ReminderUtils.getSoundUri(this, prefs, melodyPath)
        Timber.d("playMelody: $soundUri")
        soundStackHolder.sound?.playAlarm(soundUri, prefs.isInfiniteSoundEnabled, prefs.playbackDuration)
    }

    private fun playDefaultMelody() {
        val sound = soundStackHolder.sound ?: return
        Timber.d("playDefaultMelody: ")
        try {
            val afd = assets.openFd("sounds/beep.mp3")
            sound.playAlarm(afd)
        } catch (e: IOException) {
            e.printStackTrace()
            sound.playAlarm(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION), false, prefs.playbackDuration)
        }
    }

    companion object {
        const val ACTION_PLAY = Actions.ACTION_PLAY
        const val ACTION_STOP = Actions.ACTION_STOP
        private const val ARG_ID = "arg_id"
        private const val ARG_TYPE = "arg_type"

        const val TYPE_REMINDER = "type_reminder"
        const val TYPE_BIRTHDAY = "type_birthday"
        const val TYPE_MESSED = "type_missed_call"

        private val instanceCount = AtomicInteger(0)

        fun getIntent(context: Context, id: String, type: String, action: String): Intent {
            val intent = Intent(context, EventOperationalService::class.java)
            intent.action = action
            intent.putExtra(ARG_ID, id)
            intent.putExtra(ARG_TYPE, type)
            return intent
        }
    }
}