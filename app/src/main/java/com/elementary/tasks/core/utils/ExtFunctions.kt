package com.elementary.tasks.core.utils

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Deprecated("Use class scope for coroutine")
suspend fun <T> withUIContext(block: suspend CoroutineScope.() -> T): T = withContext(Dispatchers.Main, block)

@Deprecated("Use class scope for coroutine")
fun launchDefault(
  start: CoroutineStart = CoroutineStart.DEFAULT,
  block: suspend CoroutineScope.() -> Unit,
): Job = GlobalScope.launch(Dispatchers.Default, start, block)
