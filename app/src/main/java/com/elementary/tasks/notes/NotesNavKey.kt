package com.elementary.tasks.notes

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

sealed interface NotesNavKey : NavKey {
  @Serializable
  data object List : NotesNavKey

  @Serializable
  data object Archive : NotesNavKey

  /**
   * [initialStatusBarColor] snapshots [android.view.Window.getStatusBarColor] at the moment of
   * navigating to this entry (captured by the host before pushing the key, since by the time this
   * entry's own composable runs, the color may already reflect a previous recomposition). It's
   * fed straight into the destination's ViewModel so the previous screen's color can be restored
   * on the way back out, mirroring the old per-Fragment `saveStatusBarColor` capture in `onCreate`.
   */
  @Serializable
  data class Preview(val id: String, val initialStatusBarColor: Int = -1) : NotesNavKey

  @Serializable
  data class Edit(val id: String = "", val initialStatusBarColor: Int = -1) : NotesNavKey

  @Serializable
  data class ImagePreview(val position: Int, val initialStatusBarColor: Int = -1) : NotesNavKey
}
