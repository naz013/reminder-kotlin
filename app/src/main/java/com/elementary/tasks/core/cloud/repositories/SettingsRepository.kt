package com.elementary.tasks.core.cloud.repositories

import com.elementary.tasks.core.data.models.SettingsModel
import com.elementary.tasks.core.utils.Prefs
import org.koin.core.KoinComponent
import org.koin.core.inject

class SettingsRepository : DatabaseRepository<SettingsModel>(), KoinComponent {

    private val prefs: Prefs by inject()

    override suspend fun get(id: String): SettingsModel? {
        val list = prefs.all()
        return SettingsModel(list.toMutableMap())
    }

    override suspend fun insert(t: SettingsModel) {
        try {
            val prefEdit = prefs.sharedPrefs().edit()
            val entries = t.data
            for ((key, v) in entries) {
                when (v) {
                    is Boolean -> prefEdit.putBoolean(key, v)
                    is Float -> prefEdit.putFloat(key, v)
                    is Int -> prefEdit.putInt(key, v)
                    is Long -> prefEdit.putLong(key, v)
                    is String -> prefEdit.putString(key, v)
                }
            }
            prefEdit.apply()
        } catch (e: Exception) {
        }
    }

    override suspend fun all(): List<SettingsModel> {
        val item = get("") ?: return listOf()
        return listOf(item)
    }
}