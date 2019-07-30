package com.elementary.tasks.core.cloud.storages

import com.google.gson.Gson
import org.json.JSONObject
import timber.log.Timber
import java.util.concurrent.locks.ReentrantReadWriteLock

class IndexDataFile {

    private var jsonObject: JSONObject = JSONObject()
    private val lock = ReentrantReadWriteLock()
    var isLoaded = false
        private set

    fun addIndex(fileIndex: FileIndex) {
        lock.writeLock().lock()
        jsonObject.put(fileIndex.id, Gson().toJson(fileIndex))
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
        lock.readLock().lock()
        val json = jsonObject.toString()
        lock.readLock().unlock()
        Timber.d("toJson: $json")
        return json
    }

    companion object {
        const val FILE_NAME = "index.json"
    }
}