package com.elementary.tasks.core.controller

interface EventControl {

    val isActive: Boolean

    fun start(): Boolean

    fun stop(): Boolean

    fun pause(): Boolean

    fun skip(): Boolean

    fun resume(): Boolean

    operator fun next(): Boolean

    fun onOff(): Boolean

    fun canSkip(): Boolean

    fun setDelay(delay: Int)

    fun calculateTime(isNew: Boolean): Long
}
