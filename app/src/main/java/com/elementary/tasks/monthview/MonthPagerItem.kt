package com.elementary.tasks.monthview

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class MonthPagerItem(
  var monthValue: Int,
  var year: Int
) : Parcelable
