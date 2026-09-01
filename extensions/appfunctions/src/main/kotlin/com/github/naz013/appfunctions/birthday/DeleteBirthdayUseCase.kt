package com.github.naz013.appfunctions.birthday

import com.github.naz013.domain.Birthday
import com.github.naz013.repository.BirthdayRepository

class DeleteBirthdayUseCase(
  private val birthdayRepository: BirthdayRepository,
) {
  suspend operator fun invoke(id: String): Birthday? {
    val existing = birthdayRepository.getById(id) ?: return null
    birthdayRepository.delete(id)
    return existing
  }
}
