package com.elementary.tasks.month_view

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class MonthPagerItem(
        var month: Int,
        var year: Int
) : Parcelable
