package com.github.naz013.repository

import com.github.naz013.domain.RecentQuery

interface RecentQueryRepository {
  suspend fun save(recentQuery: RecentQuery)

  suspend fun getById(id: Long): RecentQuery?
  suspend fun getByQuery(query: String): RecentQuery?
  suspend fun search(query: String): List<RecentQuery>

  suspend fun getAll(): List<RecentQuery>

  suspend fun delete(id: Long)
  suspend fun deleteAll()
}
