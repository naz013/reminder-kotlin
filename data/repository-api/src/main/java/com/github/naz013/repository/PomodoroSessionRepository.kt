package com.github.naz013.repository

import com.github.naz013.domain.pomodoro.PomodoroSessionRecord
import org.threeten.bp.LocalDate

interface PomodoroSessionRepository {
  suspend fun save(record: PomodoroSessionRecord)

  suspend fun getByDateRange(from: LocalDate, to: LocalDate): List<PomodoroSessionRecord>

  suspend fun getByReminderId(reminderId: String): List<PomodoroSessionRecord>

  suspend fun getTotalFocusSecondsByReminderId(reminderId: String): Int

  suspend fun deleteAll()
}
