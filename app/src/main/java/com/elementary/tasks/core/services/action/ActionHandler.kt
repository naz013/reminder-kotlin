package com.elementary.tasks.core.services.action

fun interface ActionHandler<T> {
  suspend fun handle(data: T)
}
