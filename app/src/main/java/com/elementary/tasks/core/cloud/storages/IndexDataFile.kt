package com.elementary.tasks.core.cloud.storages

import com.google.gson.Gson
import org.json.JSONObject
import timber.log.Timber

class IndexDataFile {

    private var jsonObject: JSONObject = JSONObject()
    var isLoaded = false
        private set

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
            fileIndex == null || fileIndex.updatedAt != updatedAt
        } catch (e: Exception) {
            true
        }
    }

    fun hasIndex(id: String): Boolean {
        return jsonObject.has(id)
    }

    fun parse(json: String?) {
        Timber.d("parse: $json")
        if (json == null) {
            isLoaded = true
            return
        }
        jsonObject = try {
            JSONObject(json)
        } catch (e: Exception) {
            JSONObject()
        }
        isLoaded = true
    }

    fun toJson(): String? {
        return jsonObject.toString()
    }

    companion object {
        const val FILE_NAME = "index.json"
    }
}