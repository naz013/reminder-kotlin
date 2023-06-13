package com.elementary.tasks.core.data.repository

import com.elementary.tasks.core.data.dao.RecurPresetDao
import com.elementary.tasks.core.data.models.RecurPreset

class RecurPresetRepository(private val recurPresetDao: RecurPresetDao) {

  fun save(recurPreset: RecurPreset) {
    recurPresetDao.insert(recurPreset)
  }

  fun getAll(): List<RecurPreset> {
    return recurPresetDao.getAll()
  }

  fun getById(id: String): RecurPreset? {
    return recurPresetDao.getById(id)
  }

  fun delete(recurPreset: RecurPreset) {
    recurPresetDao.delete(recurPreset)
  }

  fun deleteById(id: String) {
    recurPresetDao.deleteById(id)
  }
}
