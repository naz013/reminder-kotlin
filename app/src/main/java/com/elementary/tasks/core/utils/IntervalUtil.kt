package com.elementary.tasks.core.utils

import java.util.*

object IntervalUtil {

    private const val REPEAT_CODE_ONCE = 0
    const val INTERVAL_DAY = 1
    private const val INTERVAL_WEEK = INTERVAL_DAY * 7
    private const val INTERVAL_TWO_WEEKS = INTERVAL_WEEK * 2
    private const val INTERVAL_THREE_WEEKS = INTERVAL_WEEK * 3
    private const val INTERVAL_FOUR_WEEKS = INTERVAL_WEEK * 4

    fun getWeekRepeat(mon: Boolean, tue: Boolean, wed: Boolean, thu: Boolean, fri: Boolean,
                      sat: Boolean, sun: Boolean): List<Int> {
        val sb = ArrayList<Int>(7)
        sb.add(0, if (sun) 1 else 0)
        sb.add(1, if (mon) 1 else 0)
        sb.add(2, if (tue) 1 else 0)
        sb.add(3, if (wed) 1 else 0)
        sb.add(4, if (thu) 1 else 0)
        sb.add(5, if (fri) 1 else 0)
        sb.add(6, if (sat) 1 else 0)
        return sb
    }

    fun isWeekday(weekday: List<Int>?): Boolean {
        if (weekday == null) return false
        for (day in weekday) {
            if (day == ReminderUtils.DAY_CHECKED) {
                return true
            }
        }
        return false
    }

    fun getBeforeTime(millis: Long, function: (PatternType) -> String): String {
        if (millis / TimeCount.DAY > 0L) {
            return if (millis / TimeCount.WEEK > 0L) {
                String.format(function.invoke(PatternType.WEEKS), (millis / TimeCount.WEEK).toString())
            } else {
                String.format(function.invoke(PatternType.DAYS), (millis / TimeCount.DAY).toString())
            }
        } else {
            return if (millis / TimeCount.HOUR > 0L) {
                String.format(function.invoke(PatternType.HOURS), (millis / TimeCount.HOUR).toString())
            } else {
                if (millis / TimeCount.MINUTE > 0L) {
                    String.format(function.invoke(PatternType.MINUTES), (millis / TimeCount.MINUTE).toString())
                } else {
                    String.format(function.invoke(PatternType.SECONDS), (millis / TimeCount.SECOND).toString())
                }
            }
        }
    }

    fun getInterval(millis: Long, function: (PatternType) -> String): String {
        var code = millis
        val tmp = millis / TimeCount.MINUTE
        val interval: String
        when {
            tmp > 1000 -> {
                code /= TimeCount.DAY
                interval = when (code) {
                    REPEAT_CODE_ONCE.toLong() -> "0"
                    INTERVAL_WEEK.toLong() -> String.format(function.invoke(PatternType.WEEKS), 1.toString())
                    INTERVAL_TWO_WEEKS.toLong() -> String.format(function.invoke(PatternType.WEEKS), 2.toString())
                    INTERVAL_THREE_WEEKS.toLong() -> String.format(function.invoke(PatternType.WEEKS), 3.toString())
                    INTERVAL_FOUR_WEEKS.toLong() -> String.format(function.invoke(PatternType.WEEKS), 4.toString())
                    else -> String.format(function.invoke(PatternType.DAYS), code.toString())
                }
            }
            tmp > 100 -> return if (code % TimeCount.HOUR == 0L) {
                code /= TimeCount.HOUR
                String.format(function.invoke(PatternType.HOURS), code.toString())
            } else {
                String.format(function.invoke(PatternType.MINUTES), tmp.toString())
            }
            else -> return if (tmp == 0L) {
                "0"
            } else {
                String.format(function.invoke(PatternType.MINUTES), tmp.toString())
            }
        }
        return interval
    }

    enum class PatternType {
        SECONDS, MINUTES, HOURS, DAYS, WEEKS
    }
}
