package com.github.naz013.logic.notificationaction.birthday

import com.github.naz013.datecalc.DateTimeManager
import com.github.naz013.domain.Birthday
import com.github.naz013.logic.birthday.SaveBirthdayUseCase
import com.github.naz013.logic.notificationaction.ActionHandler
import com.github.naz013.logic.notificationaction.CancelNotificationDecorator
import com.github.naz013.logic.notificationaction.NotificationGateway
import org.threeten.bp.LocalDate

class BirthdayCancelActionFactory(
  private val notificationGateway: NotificationGateway,
  private val dateTimeManager: DateTimeManager,
  private val saveBirthdayUseCase: SaveBirthdayUseCase,
) {
  fun createCancel(): ActionHandler<Birthday> =
    CancelNotificationDecorator(
      delegate =
      ActionHandler { birthday: Birthday ->
        saveBirthdayUseCase(
          birthday.copy(
            updatedAt = dateTimeManager.getNowGmtDateTime(),
            showedYear = LocalDate.now().year,
          ),
        )
      },
      notificationGateway = notificationGateway,
      uniqueId = Birthday::uniqueId,
    )
}
