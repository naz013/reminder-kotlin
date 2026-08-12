package com.github.naz013.appfunctions.birthday

import com.github.naz013.appfunctions.toThreeTen
import com.github.naz013.datecalc.DateTimeManager
import com.github.naz013.domain.Birthday
import com.github.naz013.domain.sync.SyncState
import com.github.naz013.repository.BirthdayRepository
import java.time.LocalDate as JavaLocalDate

class CreateSimpleBirthdayUseCase(
  private val birthdayRepository: BirthdayRepository,
  private val dateTimeManager: DateTimeManager,
) {
  suspend operator fun invoke(
    name: String,
    date: JavaLocalDate,
    ignoreYear: Boolean,
  ): Birthday {
    val birthDate = date.toThreeTen()
    val birthday =
      Birthday(
        name = name,
        date = dateTimeManager.formatBirthdayDate(birthDate),
        day = birthDate.dayOfMonth,
        month = birthDate.monthValue - 1,
        dayMonth = "${birthDate.dayOfMonth}|${birthDate.monthValue - 1}",
        updatedAt = dateTimeManager.getNowGmtDateTime(),
        ignoreYear = ignoreYear,
        syncState = SyncState.WaitingForUpload,
        version = 0,
      )
    birthdayRepository.save(birthday)
    return birthday
  }
}
