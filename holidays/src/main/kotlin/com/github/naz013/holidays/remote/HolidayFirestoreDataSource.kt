package com.github.naz013.holidays.remote

import com.github.naz013.domain.PublicHoliday
import com.github.naz013.logging.Logger
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

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

      @Suppress("UNCHECKED_CAST")
      val rawHolidays = snapshot.get(FIELD_HOLIDAYS) as? List<Map<String, Any?>> ?: emptyList()
      val holidays = rawHolidays.mapNotNull { it.toPublicHolidayOrNull(countryCode) }

      Logger.i(TAG, "Fetched ${holidays.size} holidays for $countryCode/$year")
      Result.success(holidays)
    } catch (e: Exception) {
      Logger.e(TAG, "Failed to fetch holidays for $countryCode/$year", e)
      Result.failure(e)
    }
  }

  companion object {
    private const val TAG = "HolidayFirestoreDataSource"
    private const val COLLECTION = "holidays"
    private const val DOCUMENT = "data"
    private const val FIELD_HOLIDAYS = "holidays"
  }
}
