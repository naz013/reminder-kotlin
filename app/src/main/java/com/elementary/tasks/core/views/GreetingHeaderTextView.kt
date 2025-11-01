package com.elementary.tasks.core.views

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView
import com.elementary.tasks.R
import com.elementary.tasks.core.utils.datetime.ScheduleTimes
import org.threeten.bp.LocalTime

/**
 * A custom TextView that displays a time-appropriate greeting message.
 * Automatically updates the greeting when system time changes.
 */
class GreetingHeaderTextView : AppCompatTextView {

  private var timeChangeReceiver: BroadcastReceiver? = null

  constructor(context: Context) : super(context) {
    init(context)
  }

  constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
    init(context)
  }

  constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(
    context,
    attrs,
    defStyle
  ) {
    init(context)
  }

  private fun init(context: Context) {
    updateGreeting()
  }

  override fun onAttachedToWindow() {
    super.onAttachedToWindow()
    registerTimeReceiver()
  }

  override fun onDetachedFromWindow() {
    super.onDetachedFromWindow()
    unregisterTimeReceiver()
  }

  /**
   * Registers a broadcast receiver to listen for system time changes.
   * Listens to TIME_TICK (every minute) and TIME_CHANGED (manual time change) events.
   */
  private fun registerTimeReceiver() {
    if (timeChangeReceiver == null) {
      timeChangeReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
          updateGreeting()
        }
      }

      val filter = IntentFilter().apply {
        addAction(Intent.ACTION_TIME_TICK)
        addAction(Intent.ACTION_TIME_CHANGED)
        addAction(Intent.ACTION_TIMEZONE_CHANGED)
      }

      context.registerReceiver(timeChangeReceiver, filter)
    }
  }

  /**
   * Unregisters the time change broadcast receiver.
   */
  private fun unregisterTimeReceiver() {
    timeChangeReceiver?.let {
      try {
        context.unregisterReceiver(it)
      } catch (e: IllegalArgumentException) {
        // Receiver was already unregistered, ignore
      }
      timeChangeReceiver = null
    }
  }

  /**
   * Updates the greeting text based on the current time of day.
   * Morning: 00:00 - 11:59
   * Afternoon: 12:00 - 17:59
   * Evening: 18:00 - 23:59
   */
  private fun updateGreeting() {
    val nowTime = LocalTime.now()
    val greetingText = when {
      nowTime >= ScheduleTimes.MORNING && nowTime < ScheduleTimes.NOON -> {
        context.getString(R.string.schedule_good_morning)
      }
      nowTime >= ScheduleTimes.NOON && nowTime < ScheduleTimes.EVENING -> {
        context.getString(R.string.schedule_good_afternoon)
      }
      else -> {
        context.getString(R.string.schedule_good_evening)
      }
    }

    text = greetingText
  }
}
