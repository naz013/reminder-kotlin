package com.elementary.tasks.calendar.dayview

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import org.threeten.bp.LocalDate

@Parcelize
data class DayPagerItem(
  val date: LocalDate
) : Parcelable
