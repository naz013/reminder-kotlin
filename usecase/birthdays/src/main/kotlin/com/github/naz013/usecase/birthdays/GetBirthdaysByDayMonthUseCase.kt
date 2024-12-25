package com.github.naz013.usecase.birthdays

import com.github.naz013.domain.Birthday
import com.github.naz013.repository.BirthdayRepository

class GetBirthdaysByDayMonthUseCase(
  private val birthdayRepository: BirthdayRepository
) {
  suspend operator fun invoke(
    day: Int,
    month: Int
  ): List<Birthday> {
    return birthdayRepository.getByDayMonth(day, month)
  }
}
