package com.elementary.tasks.core.views

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView
import com.elementary.tasks.R
import com.elementary.tasks.core.utils.datetime.ScheduleTimes
import org.threeten.bp.LocalTime

class GreetingHeaderTextView : AppCompatTextView {

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
