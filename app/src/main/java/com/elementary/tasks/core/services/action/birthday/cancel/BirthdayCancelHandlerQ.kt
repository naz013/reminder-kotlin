package com.elementary.tasks.core.services.action.birthday.cancel

import com.elementary.tasks.birthdays.usecase.SaveBirthdayUseCase
import com.elementary.tasks.core.services.action.ActionHandler
import com.elementary.tasks.core.utils.Notifier
import com.github.naz013.common.datetime.DateTimeManager
import com.github.naz013.domain.Birthday
import org.threeten.bp.LocalDate

class BirthdayCancelHandlerQ(
  private val notifier: Notifier,
  private val dateTimeManager: DateTimeManager,
  private val saveBirthdayUseCase: SaveBirthdayUseCase
) : ActionHandler<Birthday> {

  override suspend fun handle(data: Birthday) {
    saveBirthdayUseCase(
      data.copy(
        updatedAt = dateTimeManager.getNowGmtDateTime(),
        showedYear = LocalDate.now().year
      )
    )
    notifier.cancel(data.uniqueId)
  }
}
