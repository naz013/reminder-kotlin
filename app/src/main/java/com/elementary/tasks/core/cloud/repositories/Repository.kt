package com.elementary.tasks.core.cloud.repositories

interface Repository<T> {
    suspend fun get(id: String): T?

    suspend fun insert(t: T)
}