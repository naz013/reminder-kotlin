package com.elementary.tasks.core.cloud.repositories

interface Repository<T> {
    fun get(id: String): T?

    fun insert(t: T)
}