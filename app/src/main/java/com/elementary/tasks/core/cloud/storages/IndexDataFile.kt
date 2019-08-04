package com.elementary.tasks.core.cloud.storages

import android.text.TextUtils
import com.google.gson.GsonBuilder
import org.json.JSONObject
import timber.log.Timber
import java.util.concurrent.locks.ReentrantReadWriteLock


class IndexDataFile {

    private var jsonObject: JSONObject = JSONObject()
    private val lock = ReentrantReadWriteLock()
    private val gson = GsonBuilder()
            .excludeFieldsWithoutExposeAnnotation()
            .create()
    var isLoaded = false
        private set

    fun addIndex(fileIndex: FileIndex) {
        lock.writeLock().lock()
        jsonObject.put(fileIndex.id, gson.toJson(fileIndex))
        lock.writeLock().unlock()
    }

    fun removeIndex(id: String) {
        if (hasIndex(id)) {
            lock.writeLock().lock()
            jsonObject.remove(id)
            lock.writeLock().unlock()
        }
    }

    fun isFileChanged(id: String, updatedAt: String): Boolean {
        if (!hasIndex(id)) return true
        return try {
            val fileIndex = gson.fromJson(jsonObject.getJSONObject(id).toString(), FileIndex::class.java)
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
        lock.readLock().lock()
        for (key in jsonObject.keys()) {
            try {
                val fileIndex = gson.fromJson(jsonObject.getJSONObject(key).toString(), FileIndex::class.java)
                if (fileIndex != null && !TextUtils.isEmpty(fileIndex.json)) {
                    jsonObject.put(fileIndex.id, gson.toJson(fileIndex))
                }
            } catch (e: Exception) {
            }
        }
        val json = jsonObject.toString()
        lock.readLock().unlock()
        Timber.d("toJson: $json")
        return json
    }

    companion object {
        const val FILE_NAME = "index.json"
    }
}