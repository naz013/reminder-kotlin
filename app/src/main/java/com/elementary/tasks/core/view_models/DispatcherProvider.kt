package com.elementary.tasks.core.view_models

import kotlinx.coroutines.Dispatchers

class DispatcherProvider {
    fun main() = Dispatchers.Main
    fun io() = Dispatchers.IO
    fun default() = Dispatchers.Default
}