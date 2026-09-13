package com.github.naz013.feature.pomodoro

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

sealed interface PomodoroNavKey : NavKey {
  @Serializable
  data class Timer(val linkedReminderId: String? = null) : PomodoroNavKey
}
