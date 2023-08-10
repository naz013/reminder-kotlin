package com.elementary.tasks.dayview

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class EventsPagerItem(
  var day: Int,
  var month: Int,
  var year: Int,
  var isToday: Boolean = false
) : Parcelable
