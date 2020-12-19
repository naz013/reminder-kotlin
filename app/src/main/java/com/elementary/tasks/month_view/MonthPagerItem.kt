package com.elementary.tasks.month_view

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class MonthPagerItem(
  var month: Int,
  var year: Int
) : Parcelable
