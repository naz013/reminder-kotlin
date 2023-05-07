package com.elementary.tasks.core.services.action

interface ActionHandler<T> {
  fun handle(data: T)
}
