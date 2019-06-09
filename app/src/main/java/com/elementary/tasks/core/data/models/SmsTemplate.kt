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
class SmsTemplate : Serializable {
    @SerializedName("title")
    var title: String = ""
    @SerializedName("key")
    @PrimaryKey
    var key: String = ""
    @SerializedName("date")
    var date: String = ""
    @Transient
    var isSelected: Boolean = false

    constructor() {
        this.key = UUID.randomUUID().toString()
    }

    @Ignore
    constructor(title: String, date: String) {
        this.title = title
        this.date = date
        this.key = UUID.randomUUID().toString()
    }

    @Ignore
    constructor(title: String) {
        this.title = title
        this.date = TimeUtil.gmtDateTime
        this.key = UUID.randomUUID().toString()
    }
}
