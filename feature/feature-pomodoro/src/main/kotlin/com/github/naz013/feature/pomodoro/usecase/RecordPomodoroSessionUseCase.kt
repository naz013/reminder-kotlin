package com.github.naz013.feature.pomodoro.usecase

import com.github.naz013.domain.pomodoro.PomodoroSessionRecord
import com.github.naz013.repository.PomodoroSessionRepository
import org.threeten.bp.LocalDateTime

internal class RecordPomodoroSessionUseCase(
  private val pomodoroSessionRepository: PomodoroSessionRepository
) {
  suspend operator fun invoke(
    linkedReminderId: String?,
    startedAt: LocalDateTime,
    plannedFocusSeconds: Int,
    actualFocusSeconds: Int,
    wasCompleted: Boolean,
  ) {
    pomodoroSessionRepository.save(
      PomodoroSessionRecord(
        linkedReminderId = linkedReminderId,
        startedAt = startedAt,
        plannedFocusSeconds = plannedFocusSeconds,
        actualFocusSeconds = actualFocusSeconds,
        wasCompleted = wasCompleted,
      )
    )
  }
}
