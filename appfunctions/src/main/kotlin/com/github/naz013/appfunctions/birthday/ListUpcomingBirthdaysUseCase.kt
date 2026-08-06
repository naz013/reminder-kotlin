package com.github.naz013.appfunctions.birthday

import com.github.naz013.common.datetime.DateTimeManager
import com.github.naz013.datecalc.BirthdayDateCalculator
import com.github.naz013.domain.Birthday
import com.github.naz013.usecase.birthdays.GetAllBirthdaysUseCase
import org.threeten.bp.LocalTime

class ListUpcomingBirthdaysUseCase(
  private val getAllBirthdaysUseCase: GetAllBirthdaysUseCase,
  private val birthdayDateCalculator: BirthdayDateCalculator,
  private val dateTimeManager: DateTimeManager,
) {
  suspend operator fun invoke(withinDays: Int): List<Birthday> {
    val now = dateTimeManager.getCurrentDateTime()
    val birthdayTime = dateTimeManager.getBirthdayLocalTime() ?: LocalTime.MIDNIGHT
    val limit = now.plusDays(withinDays.toLong())

    return getAllBirthdaysUseCase()
      .mapNotNull { birthday ->
        val birthDate = dateTimeManager.parseBirthdayDate(birthday.date) ?: return@mapNotNull null
        val nextOccurrence =
          birthdayDateCalculator.getNextOccurrence(
            birthDate = birthDate,
            birthdayTime = birthdayTime,
            ignoreYear = birthday.ignoreYear,
            showedYear = birthday.showedYear,
            nowDateTime = now,
          )
        birthday to nextOccurrence
      }
      .filter { (_, nextOccurrence) -> !nextOccurrence.isAfter(limit) }
      .sortedBy { (_, nextOccurrence) -> nextOccurrence }
      .map { (birthday, _) -> birthday }
  }
}
