package com.elementary.tasks.notes.list

import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.elementary.tasks.AdsProvider
import com.elementary.tasks.core.data.models.NoteWithImages
import com.elementary.tasks.core.interfaces.ActionsListener
import com.elementary.tasks.core.utils.Module

class NotesRecyclerAdapter(private val refreshListener: () -> Unit)
    : ListAdapter<NoteWithImages, RecyclerView.ViewHolder>(NoteDIffCallback()) {

    var actionsListener: ActionsListener<NoteWithImages>? = null
    private val adsProvider = AdsProvider()

    override fun submitList(list: List<NoteWithImages>?) {
        super.submitList(list)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            AdsProvider.ADS_VIEW_TYPE -> NoteAdsHolder(parent, adsProvider, refreshListener)
            else -> NoteHolder(parent) { view, i, listActions ->
                if (actionsListener != null) {
                    actionsListener?.onAction(view, i, getItem(i), listActions)
                }
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is NoteHolder) {
            holder.setData(getItem(position))
        }
    }

    override fun getItemViewType(position: Int): Int {
        val item = getItem(position)
        return if (!Module.isPro && item.getKey() == AdsProvider.NOTE_BANNER_ID) {
            AdsProvider.ADS_VIEW_TYPE
        } else {
            0
        }
    }

    fun onDestroy() {
        adsProvider.destroy()
    }
}
