package com.github.naz013.holidays.remote

import com.github.naz013.domain.PublicHoliday
import com.github.naz013.logging.Logger
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import org.threeten.bp.LocalDate

/**
 * Reads `holidays/{countryCode}/{year}/data` from Firestore. Mirrors
 * `reviews/src/main/kotlin/com/github/naz013/reviews/db/FirestoreDatabase.kt`'s shape:
 * internal class, [Result] returns, try/catch + [Logger].
 */
internal class HolidayFirestoreDataSource(
  private val firestore: FirebaseFirestore,
) {
  suspend fun fetch(countryCode: String, year: Int): Result<List<PublicHoliday>> {
    return try {
      val snapshot = firestore
        .collection(COLLECTION)
        .document(countryCode)
        .collection(year.toString())
        .document(DOCUMENT)
        .get()
        .await()

      if (!snapshot.exists()) {
        Logger.w(TAG, "No holiday document for $countryCode/$year")
        return Result.success(emptyList())
      }

      val document = snapshot.toObject(HolidayDocumentDto::class.java)
      val holidays = document?.holidays.orEmpty().mapNotNull { it.toDomainOrNull(countryCode) }

      Logger.i(TAG, "Fetched ${holidays.size} holidays for $countryCode/$year")
      Result.success(holidays)
    } catch (e: Exception) {
      Logger.e(TAG, "Failed to fetch holidays for $countryCode/$year", e)
      Result.failure(e)
    }
  }

  private fun HolidayItemDto.toDomainOrNull(countryCode: String): PublicHoliday? {
    val holidayName = name?.takeIf { it.isNotBlank() } ?: return null
    val parsedDate = date?.let { runCatching { LocalDate.parse(it) }.getOrNull() } ?: return null
    return PublicHoliday(
      id = PublicHoliday.id(countryCode, parsedDate, holidayName),
      countryCode = countryCode,
      date = parsedDate,
      name = holidayName,
      nameLocal = nameLocal,
      type = type.orEmpty(),
      location = location,
    )
  }

  companion object {
    private const val TAG = "HolidayFirestoreDataSource"
    private const val COLLECTION = "holidays"
    private const val DOCUMENT = "data"
  }
}
