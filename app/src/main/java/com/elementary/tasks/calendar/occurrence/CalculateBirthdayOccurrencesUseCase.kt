package com.elementary.tasks.calendar.occurrence

import com.elementary.tasks.core.utils.params.Prefs
import com.github.naz013.common.datetime.DateTimeManager
import com.github.naz013.domain.occurance.EventOccurrence
import com.github.naz013.domain.occurance.OccurrenceType
import com.github.naz013.logging.Logger
import com.github.naz013.repository.BirthdayRepository
import com.github.naz013.repository.EventOccurrenceRepository
import org.threeten.bp.LocalTime
import java.util.UUID

class CalculateBirthdayOccurrencesUseCase(
  private val prefs: Prefs,
  private val birthdayRepository: BirthdayRepository,
  private val dateTimeManager: DateTimeManager,
  private val eventOccurrenceRepository: EventOccurrenceRepository
) {

  suspend operator fun invoke(id: String) {
    val birthday = birthdayRepository.getById(id) ?: run {
      Logger.w(TAG, "Birthday not found: $id")
      return
    }
    val date = dateTimeManager.parseBirthdayDate(birthday.date) ?: run {
      Logger.w(TAG, "Birthday date parse error: ${birthday.date}")
      return
    }
    eventOccurrenceRepository.deleteByEventId(birthday.uuId)
    val time = dateTimeManager.getBirthdayLocalTime() ?: LocalTime.now()
    val previousYear = dateTimeManager.getCurrentDate().year - 1
    val startDate = date.withYear(previousYear)
    for (i in 0..prefs.numberOfBirthdayOccurrences) {
      val occurrenceDate = startDate.plusYears(i.toLong())
      eventOccurrenceRepository.save(
        EventOccurrence(
          id = UUID.randomUUID().toString(),
          eventId = birthday.uuId,
          date = occurrenceDate,
          time = time,
          type = OccurrenceType.Birthday,
        )
      )
    }
    Logger.i(TAG, "Birthday occurrences calculated for birthday: ${birthday.uuId}")
  }

  companion object {
    private const val TAG = "CalculateBirthdayOccurrencesUseCase"
  }
}
