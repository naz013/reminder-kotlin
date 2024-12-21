package com.elementary.tasks.core.services.action

interface ActionHandler<T> {
  suspend fun handle(data: T)
}
