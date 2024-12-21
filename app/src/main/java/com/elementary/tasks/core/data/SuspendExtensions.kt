package com.elementary.tasks.core.data

import kotlinx.coroutines.runBlocking

fun <T> invokeSuspend(block: suspend () -> T): T {
  return runBlocking { block() }
}
