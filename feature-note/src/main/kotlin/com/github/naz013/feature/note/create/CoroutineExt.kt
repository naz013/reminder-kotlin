package com.github.naz013.feature.note.create

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

suspend fun <T> withUIContext(block: suspend CoroutineScope.() -> T): T = withContext(Dispatchers.Main, block)
