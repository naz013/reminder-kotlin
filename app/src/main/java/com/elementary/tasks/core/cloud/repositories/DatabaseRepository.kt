package com.elementary.tasks.core.cloud.repositories

import com.elementary.tasks.core.data.AppDb
import org.koin.core.component.KoinApiExtension
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

@KoinApiExtension
abstract class DatabaseRepository<T> : Repository<T>, KoinComponent {
  protected val appDb: AppDb by inject()
}