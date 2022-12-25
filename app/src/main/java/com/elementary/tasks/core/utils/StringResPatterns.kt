package com.elementary.tasks.core.utils

import android.content.Context
import com.elementary.tasks.R
import com.elementary.tasks.core.utils.datetime.IntervalUtil

object StringResPatterns {

  fun getIntervalPattern(context: Context, type: IntervalUtil.PatternType): String {
    return when (type) {
      IntervalUtil.PatternType.SECONDS -> "0"
      IntervalUtil.PatternType.MINUTES -> context.getString(R.string.x_min)
      IntervalUtil.PatternType.HOURS -> context.getString(R.string.x_hours)
      IntervalUtil.PatternType.DAYS -> context.getString(R.string.xD)
      IntervalUtil.PatternType.WEEKS -> context.getString(R.string.xW)
    }
  }

  fun getBeforePattern(context: Context, type: IntervalUtil.PatternType): String {
    return when (type) {
      IntervalUtil.PatternType.SECONDS -> context.getString(R.string.x_seconds)
      IntervalUtil.PatternType.MINUTES -> context.getString(R.string.x_minutes)
      IntervalUtil.PatternType.HOURS -> context.getString(R.string.x_hours)
      IntervalUtil.PatternType.DAYS -> context.getString(R.string.x_days)
      IntervalUtil.PatternType.WEEKS -> context.getString(R.string.x_weeks)
    }
  }
}