package com.elementary.tasks.core.os

import android.app.ActivityManager
import android.app.KeyguardManager
import android.app.NotificationManager
import android.app.SearchManager
import android.content.ClipboardManager
import android.content.Context
import android.location.LocationManager
import android.media.AudioManager
import android.os.PowerManager
import android.telephony.TelephonyManager
import android.view.inputmethod.InputMethodManager

class SystemServiceProvider(context: Context) {

  private val appContext = context.applicationContext

  fun provideNotificationManager(): NotificationManager? {
    return appContext.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager?
  }

  fun provideLocationManager(): LocationManager? {
    return appContext?.getSystemService(Context.LOCATION_SERVICE) as? LocationManager?
  }

  fun provideInputMethodManager(): InputMethodManager? {
    return appContext?.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager?
  }

  fun provideSearchManager(): SearchManager? {
    return appContext?.getSystemService(Context.SEARCH_SERVICE) as? SearchManager?
  }

  @Deprecated("After R")
  fun provideTelephonyManager(): TelephonyManager? {
    return appContext?.getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager?
  }

  fun provideAudioManager(): AudioManager? {
    return appContext?.getSystemService(Context.AUDIO_SERVICE) as? AudioManager?
  }

  fun provideClipboardManager(): ClipboardManager? {
    return appContext?.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager?
  }

  fun provideActivityManager(): ActivityManager? {
    return appContext?.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager?
  }

  fun provideKeyguardManager(): KeyguardManager? {
    return appContext?.getSystemService(Context.KEYGUARD_SERVICE) as? KeyguardManager?
  }

  fun providePowerManager(): PowerManager? {
    return appContext?.getSystemService(Context.POWER_SERVICE) as? PowerManager?
  }
}
