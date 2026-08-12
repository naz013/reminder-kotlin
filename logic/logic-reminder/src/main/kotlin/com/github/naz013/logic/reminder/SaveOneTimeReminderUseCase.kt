package com.github.naz013.logic.reminder

import org.threeten.bp.LocalDateTime
import java.util.UUID

interface SaveOneTimeReminderUseCase {
  suspend operator fun invoke(
    uuId: String = UUID.randomUUID().toString(),
    summary: String? = null,
    dateTime: LocalDateTime,
  ): String
}
