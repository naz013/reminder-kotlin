package com.github.naz013.repository.impl

import com.github.naz013.domain.pomodoro.PomodoroSessionRecord
import com.github.naz013.logging.Logger
import com.github.naz013.repository.PomodoroSessionRepository
import com.github.naz013.repository.dao.PomodoroSessionDao
import com.github.naz013.repository.entity.toDomain
import com.github.naz013.repository.entity.toEntity
import com.github.naz013.repository.entity.toEpochMillisUtc
import com.github.naz013.repository.observer.TableChangeNotifier
import com.github.naz013.repository.table.Table
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalTime

internal class PomodoroSessionRepositoryImpl(
  private val dao: PomodoroSessionDao,
  private val tableChangeNotifier: TableChangeNotifier
) : PomodoroSessionRepository {

  private val table = Table.PomodoroSession

  override suspend fun save(record: PomodoroSessionRecord) {
    Logger.d(TAG, "Save pomodoro session: ${record.id}")
    dao.insert(record.toEntity())
    tableChangeNotifier.notify(table)
  }

  override suspend fun getByDateRange(from: LocalDate, to: LocalDate): List<PomodoroSessionRecord> {
    Logger.d(TAG, "Get pomodoro sessions by date range: $from - $to")
    val fromMillis = from.atTime(LocalTime.MIN).toEpochMillisUtc()
    val toMillis = to.atTime(LocalTime.MAX).toEpochMillisUtc()
    return dao.getByDateRange(fromMillis, toMillis).map { it.toDomain() }
  }

  override suspend fun getByReminderId(reminderId: String): List<PomodoroSessionRecord> {
    Logger.d(TAG, "Get pomodoro sessions by reminderId: $reminderId")
    return dao.getByReminderId(reminderId).map { it.toDomain() }
  }

  override suspend fun getTotalFocusSecondsByReminderId(reminderId: String): Int {
    return dao.getTotalFocusSecondsByReminderId(reminderId)
  }

  override suspend fun deleteAll() {
    Logger.d(TAG, "Delete all pomodoro sessions")
    dao.deleteAll()
    tableChangeNotifier.notify(table)
  }

  companion object {
    private const val TAG = "PomodoroSessionRepository"
  }
}
