package com.elementary.tasks.core.cloud.storages

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class FileIndex(
        @SerializedName("ext")
        var ext: String = "",
        @SerializedName("updatedAt")
        var updatedAt: String = "",
        @SerializedName("id")
        var id: String = "",
        @SerializedName("attachment")
        var attachment: String = "",
        @SerializedName("melody")
        var melody: String = "",
        @SerializedName("type")
        var type: String = "",
        @SerializedName("json")
        var json: String? = null
)