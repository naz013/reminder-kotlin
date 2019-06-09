package com.elementary.tasks.core.data.models

import com.elementary.tasks.core.utils.TimeUtil
import com.google.gson.annotations.SerializedName
import java.io.Serializable
import java.util.*

class ShopItem : Serializable {

    @SerializedName("summary")
    var summary: String = ""
    @SerializedName("isDeleted")
    var isDeleted = false
    @SerializedName("checked")
    var isChecked = false
    @SerializedName("uuId")
    var uuId: String = ""
    @SerializedName("createTime")
    var createTime: String = ""

    constructor(summary: String) {
        this.summary = summary
        this.createTime = TimeUtil.gmtDateTime
        this.uuId = UUID.randomUUID().toString()
    }

    constructor(summary: String, isDeleted: Boolean, checked: Boolean, uuId: String, createTime: String) {
        this.summary = summary
        this.isDeleted = isDeleted
        this.isChecked = checked
        this.uuId = uuId
        this.createTime = createTime
    }

    override fun toString(): String {
        return "ShopItem(summary='$summary', isDeleted=$isDeleted, isChecked=$isChecked, uuId='$uuId', createTime='$createTime')"
    }
}