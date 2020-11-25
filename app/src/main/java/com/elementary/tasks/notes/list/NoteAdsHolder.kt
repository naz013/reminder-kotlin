package com.elementary.tasks.notes.list

import android.view.ViewGroup
import com.elementary.tasks.AdsProvider
import com.elementary.tasks.R
import com.elementary.tasks.core.arch.BaseHolder
import com.elementary.tasks.core.data.models.Note
import com.elementary.tasks.core.data.models.NoteWithImages
import com.elementary.tasks.core.utils.Prefs
import com.elementary.tasks.core.utils.inflater
import com.elementary.tasks.databinding.ListItemNoteAdsBinding

class NoteAdsHolder(
  parent: ViewGroup,
  adsProvider: AdsProvider,
  prefs: Prefs,
  failListener: () -> Unit
) : BaseHolder<ListItemNoteAdsBinding>(
  ListItemNoteAdsBinding.inflate(parent.inflater(), parent, false),
  prefs
) {

  init {
    adsProvider.showBanner(
      binding.adsHolder,
      AdsProvider.NOTE_BANNER_ID,
      if (!prefs.isNotesGridEnabled) R.layout.list_item_ads_ver else R.layout.list_item_ads_hor,
      failListener
    )
  }

  companion object {

    fun updateList(list: List<NoteWithImages>): List<NoteWithImages> {
      return if (AdsProvider.hasAds() && list.isNotEmpty()) {
        val mutable = list.toMutableList()
        if (list.size > 6) {
          mutable.add(3, NoteWithImages().apply {
            this.note = Note(key = AdsProvider.NOTE_BANNER_ID)
          })
        } else {
          mutable.add(list.size / 2 + 1, NoteWithImages().apply {
            this.note = Note(key = AdsProvider.NOTE_BANNER_ID)
          })
        }
        mutable
      } else {
        list
      }
    }
  }
}
