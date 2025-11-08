package com.elementary.tasks.core.controller

import org.threeten.bp.LocalDateTime

interface EventControl {
  val isActive: Boolean
  fun enable(): Boolean // Covered by Use case
  fun disable(): Boolean // Covered by Use case
  fun pause(): Boolean // Covered by Use case
  fun skip(): Boolean // Covered by Use case
  fun resume(): Boolean // Covered by Use case
  operator fun next(): Boolean // Covered by Use case
  fun onOff(): Boolean
  fun canSkip(): Boolean // Covered by Use case
  fun setDelay(delay: Int) // Covered by Use case
  fun calculateTime(isNew: Boolean): LocalDateTime // Covered by Use case
  fun justStart()
}
