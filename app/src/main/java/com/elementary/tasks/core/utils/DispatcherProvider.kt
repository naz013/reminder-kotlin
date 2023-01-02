package com.elementary.tasks.core.utils

import kotlinx.coroutines.Dispatchers

class DispatcherProvider {
    fun main() = Dispatchers.Main
    fun io() = Dispatchers.IO
    fun default() = Dispatchers.Default
}