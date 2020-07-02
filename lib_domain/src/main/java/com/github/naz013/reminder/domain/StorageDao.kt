package com.github.naz013.reminder.domain

interface StorageDao<T, Id> {
    suspend fun save(t: T)

    suspend fun get(id: Id): T?

    suspend fun delete(id: Id): Boolean
}