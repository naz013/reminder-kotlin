package com.github.naz013.repository

import com.github.naz013.domain.habit.HabitCheckIn
import kotlinx.coroutines.flow.Flow
import org.threeten.bp.LocalDate

interface HabitCheckInRepository {
  fun observeRange(habitId: String, from: LocalDate, to: LocalDate): Flow<List<HabitCheckIn>>

  suspend fun getRange(habitId: String, from: LocalDate, to: LocalDate): List<HabitCheckIn>

  suspend fun get(habitId: String, date: LocalDate): HabitCheckIn?

  suspend fun save(checkIn: HabitCheckIn)

  suspend fun delete(habitId: String, date: LocalDate)

  suspend fun deleteAllForHabit(habitId: String)
}
