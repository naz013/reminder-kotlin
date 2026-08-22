package com.github.naz013.logic.notificationaction

fun interface ActionHandler<T> {
  suspend fun handle(data: T)
}
