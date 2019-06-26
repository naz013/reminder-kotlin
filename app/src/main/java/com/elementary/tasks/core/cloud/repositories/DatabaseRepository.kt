package com.elementary.tasks.core.cloud.repositories

import com.elementary.tasks.core.data.AppDb
import org.koin.core.KoinComponent
import org.koin.core.inject

abstract class DatabaseRepository<T> : Repository<T>, KoinComponent {
    protected val appDb: AppDb by inject()
}