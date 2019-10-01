package com.elementary.tasks.core.cloud.storages

import androidx.annotation.Keep
import com.elementary.tasks.core.cloud.converters.IndexTypes
import com.elementary.tasks.core.utils.CopyByteArrayStream
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

@Keep
data class FileIndex(
        @SerializedName("ext")
        @Expose
        var ext: String = "",
        @SerializedName("updatedAt")
        @Expose
        var updatedAt: String = "",
        @SerializedName("id")
        @Expose
        var id: String = "",
        @SerializedName("attachment")
        @Expose
        var attachment: String = "",
        @SerializedName("melody")
        @Expose
        var melody: String = "",
        @SerializedName("type")
        @Transient
        var type: IndexTypes = IndexTypes.TYPE_REMINDER,
        @SerializedName("stream")
        @Transient
        var stream: CopyByteArrayStream? = null,
        @SerializedName("readyToBackup")
        @Transient
        var readyToBackup: Boolean = false
) {
    fun isOk(): Boolean {
        return ext.isNotEmpty() && id.isEmpty()
    }
}