package com.github.naz013.usecase.birthdays

import com.github.naz013.domain.Birthday
import com.github.naz013.repository.BirthdayRepository

class GetAllBirthdaysUseCase(
  private val birthdayRepository: BirthdayRepository
) {
  suspend operator fun invoke(): List<Birthday> {
    return birthdayRepository.getAll()
  }
}
