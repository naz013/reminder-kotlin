package com.github.naz013.appfunctions.birthday

import com.github.naz013.domain.Birthday
import com.github.naz013.repository.BirthdayRepository

class SearchBirthdaysUseCase(
  private val birthdayRepository: BirthdayRepository,
) {
  suspend operator fun invoke(query: String): List<Birthday> = birthdayRepository.searchByName(query)
}
