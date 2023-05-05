package com.elementary.tasks.core.data.repository

import com.elementary.tasks.core.data.dao.MissedCallsDao
import com.elementary.tasks.core.data.models.MissedCall

@Deprecated("After S")
class MissedCallRepository(private val missedCallsDao: MissedCallsDao) {

  fun save(missedCall: MissedCall) {
    missedCallsDao.insert(missedCall)
  }

  fun getByNumber(phoneNumber: String): MissedCall? {
    return missedCallsDao.getByNumber(phoneNumber)
  }

  fun delete(missedCall: MissedCall) {
    missedCallsDao.delete(missedCall)
  }
}