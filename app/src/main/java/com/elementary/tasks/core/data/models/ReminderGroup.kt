package com.elementary.tasks.core.data.models

import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.elementary.tasks.core.utils.TimeUtil
import com.google.gson.annotations.SerializedName
import java.io.Serializable
import java.util.*

@Entity
@Keep
data class ReminderGroup(
        @SerializedName("title")
        var groupTitle: String = "",
        @SerializedName("uuId")
        @PrimaryKey
        var groupUuId: String = UUID.randomUUID().toString(),
        @SerializedName("color")
        var groupColor: Int = 0,
        @SerializedName("dateTime")
        var groupDateTime: String = "",
        @SerializedName("isDefaultGroup")
        var isDefaultGroup: Boolean = false) : Serializable {

    @Ignore
    constructor(title: String, color: Int) : this() {
        this.groupTitle = title
        this.groupUuId = UUID.randomUUID().toString()
        this.groupColor = color
        this.groupDateTime = TimeUtil.gmtDateTime
    }

    @Ignore
    constructor(title: String, uuId: String, color: Int, dateTime: String) : this() {
        this.groupTitle = title
        this.groupUuId = uuId
        this.groupColor = color
        this.groupDateTime = dateTime
    }
}
