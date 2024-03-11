package com.elementary.tasks.core.controller

import org.threeten.bp.LocalDateTime

interface EventControl {
  val isActive: Boolean
  fun enable(): Boolean
  fun disable(): Boolean
  fun pause(): Boolean
  fun skip(): Boolean
  fun resume(): Boolean
  operator fun next(): Boolean
  fun onOff(): Boolean
  fun canSkip(): Boolean
  fun setDelay(delay: Int)
  fun calculateTime(isNew: Boolean): LocalDateTime
  fun justStart()
}
