package com.elementary.tasks.core.data.models

import android.os.Parcelable
import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.elementary.tasks.core.utils.TimeUtil
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize
import java.util.*

@Entity
@Keep
@Parcelize
data class ReminderGroup(
        @SerializedName("title")
        var groupTitle: String = "",
        @SerializedName("uuId")
        @PrimaryKey
        var groupUuId: String = UUID.randomUUID().toString(),
        @SerializedName("color")
        var groupColor: Int = 0,
        @SerializedName("dateTime")
        var groupDateTime: String = TimeUtil.gmtDateTime,
        @SerializedName("isDefaultGroup")
        var isDefaultGroup: Boolean = false
) : Parcelable
