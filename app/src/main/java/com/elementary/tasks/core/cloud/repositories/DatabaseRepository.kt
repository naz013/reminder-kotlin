package com.elementary.tasks.core.cloud.repositories

import com.elementary.tasks.core.data.AppDb

abstract class DatabaseRepository<T>(
  protected val appDb: AppDb
) : Repository<T>
