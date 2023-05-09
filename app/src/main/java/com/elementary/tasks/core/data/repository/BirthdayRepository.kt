package com.elementary.tasks.core.data.repository

import com.elementary.tasks.core.data.dao.BirthdaysDao
import com.elementary.tasks.core.data.models.Birthday

class BirthdayRepository(private val birthdaysDao: BirthdaysDao) {

  fun save(birthday: Birthday) {
    birthdaysDao.insert(birthday)
  }

  fun getById(id: String): Birthday? {
    return birthdaysDao.getById(id)
  }

  fun getByDayMonth(day: Int, month: Int): List<Birthday> {
    return birthdaysDao.getAll("$day|$month")
  }

  fun getAll(): List<Birthday> {
    return birthdaysDao.getAll()
  }

  suspend fun delete(id: String) {
    birthdaysDao.delete(id)
  }

  suspend fun delete(birthday: Birthday) {
    birthdaysDao.delete(birthday)
  }
}
