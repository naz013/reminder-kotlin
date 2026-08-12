package com.github.naz013.feature.common.coroutine

import kotlinx.coroutines.runBlocking

fun <T> invokeSuspend(block: suspend () -> T): T {
  return runBlocking { block() }
}
