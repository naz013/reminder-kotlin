package com.elementary.tasks.core.data.models

import com.elementary.tasks.core.utils.SuperUtil
import com.elementary.tasks.core.utils.TimeUtil
import com.google.gson.annotations.SerializedName
import java.io.Serializable

import java.util.UUID

/**
 * Copyright 2016 Nazar Suhovich
 *
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
        return SuperUtil.getObjectPrint(this, ShopItem::class.java)
    }
}