package com.github.naz013.repository

import com.github.naz013.domain.UsedTime

interface UsedTimeRepository {
  suspend fun save(usedTime: UsedTime)

  suspend fun getByTimeMills(timeMills: Long): UsedTime?
  suspend fun getAll(): List<UsedTime>
  suspend fun getFirst(limit: Int): List<UsedTime>

  suspend fun delete(id: Long)
  suspend fun deleteAll()
}
