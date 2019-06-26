package com.elementary.tasks.core.cloud

import androidx.annotation.Keep
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import org.json.JSONObject

class IndexDataFile {

    private var jsonObject: JSONObject = JSONObject()

    fun addIndex(fileIndex: FileIndex) {
        jsonObject.put(fileIndex.id, Gson().toJson(fileIndex))
    }

    fun removeIndex(id: String) {
        if (hasIndex(id)) {
            jsonObject.remove(id)
        }
    }

    fun isFileChanged(id: String, updatedAt: String): Boolean {
        if (!hasIndex(id)) return true
        return try {
            val fileIndex = Gson().fromJson(jsonObject.getJSONObject(id).toString(), FileIndex::class.java)
            fileIndex.updatedAt != updatedAt
        } catch (e: Exception) {
            true
        }
    }

    fun hasIndex(id: String): Boolean {
        return jsonObject.has(id)
    }

    fun parse(json: String?) {
        if (json == null) return
        jsonObject = try {
            JSONObject(json)
        } catch (e: Exception) {
            JSONObject()
        }
    }

    fun toJson(): String? {
        return jsonObject.toString()
    }

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
            var type: String = ""
    )

    companion object {
        const val FILE_NAME = "index.json"
    }
}