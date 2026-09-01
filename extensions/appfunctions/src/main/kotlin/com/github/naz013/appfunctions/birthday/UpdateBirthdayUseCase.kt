package com.github.naz013.appfunctions.birthday

import com.github.naz013.appfunctions.toThreeTen
import com.github.naz013.datecalc.DateTimeManager
import com.github.naz013.domain.Birthday
import com.github.naz013.domain.sync.SyncState
import com.github.naz013.repository.BirthdayRepository
import java.time.LocalDate as JavaLocalDate

class UpdateBirthdayUseCase(
  private val birthdayRepository: BirthdayRepository,
  private val dateTimeManager: DateTimeManager,
) {
  suspend operator fun invoke(
    id: String,
    name: String,
    date: JavaLocalDate,
    ignoreYear: Boolean,
  ): Birthday? {
    val existing = birthdayRepository.getById(id) ?: return null
    val fields = dateTimeManager.toBirthdayDateFields(date.toThreeTen())
    val updated =
      existing.copy(
        name = name,
        date = fields.date,
        day = fields.day,
        month = fields.month,
        dayMonth = fields.dayMonth,
        updatedAt = dateTimeManager.getNowGmtDateTime(),
        ignoreYear = ignoreYear,
        version = existing.version + 1,
      )
    birthdayRepository.save(updated)
    birthdayRepository.updateSyncState(existing.uuId, SyncState.WaitingForUpload)
    return updated
  }
}
