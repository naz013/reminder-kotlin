package com.github.nsy.reviewsadmin.cache

import android.content.Context
import android.content.SharedPreferences
import com.github.naz013.logging.Logger
import com.github.naz013.reviews.AppSource

/**
 * Cache manager for review IDs to track new reviews since last check.
 *
 * Uses SharedPreferences to persistently store review IDs grouped by app source.
 * This allows the dashboard to show how many new reviews have been received
 * since the last time the data was cached.
 *
 * @property context Application context for SharedPreferences access
 */
class ReviewIdCache(private val context: Context) {

  private val prefs: SharedPreferences by lazy {
    context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
  }

  /**
   * Gets the set of cached review IDs for a specific app source.
   *
   * @param source The app source to get cached IDs for
   * @return Set of review IDs that were previously cached
   */
  fun getCachedReviewIds(source: AppSource): Set<String> {
    return try {
      val key = getKeyForSource(source)
      prefs.getStringSet(key, emptySet()) ?: emptySet()
    } catch (e: Exception) {
      Logger.e(TAG, "Error reading cached review IDs for $source", e)
      emptySet()
    }
  }

  /**
   * Saves the current set of review IDs for a specific app source.
   *
   * @param source The app source to cache IDs for
   * @param reviewIds Set of review IDs to cache
   */
  fun saveReviewIds(source: AppSource, reviewIds: Set<String>) {
    try {
      val key = getKeyForSource(source)
      prefs.edit()
        .putStringSet(key, reviewIds)
        .apply()
      Logger.i(TAG, "Cached ${reviewIds.size} review IDs for $source")
    } catch (e: Exception) {
      Logger.e(TAG, "Error saving cached review IDs for $source", e)
    }
  }

  /**
   * Clears all cached review IDs for all app sources.
   */
  fun clearAll() {
    try {
      prefs.edit().clear().apply()
      Logger.i(TAG, "Cleared all cached review IDs")
    } catch (e: Exception) {
      Logger.e(TAG, "Error clearing cached review IDs", e)
    }
  }

  /**
   * Generates the preference key for a specific app source.
   *
   * @param source The app source
   * @return The preference key string
   */
  private fun getKeyForSource(source: AppSource): String {
    return "${KEY_PREFIX}${source.name.lowercase()}"
  }

  companion object {
    private const val TAG = "ReviewIdCache"
    private const val PREFS_NAME = "review_id_cache"
    private const val KEY_PREFIX = "review_ids_"
  }
}

