package com.elementary.tasks.birthdays.list

import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.elementary.tasks.AdsProvider
import com.elementary.tasks.core.arch.CurrentStateHolder
import com.elementary.tasks.core.interfaces.ActionsListener
import com.elementary.tasks.core.utils.Module

class BirthdaysRecyclerAdapter(
  private val currentStateHolder: CurrentStateHolder,
  private val refreshListener: () -> Unit
) : ListAdapter<BirthdayListItem, RecyclerView.ViewHolder>(BirthdayDiffCallback()) {

  var actionsListener: ActionsListener<BirthdayListItem>? = null
  private val adsProvider = AdsProvider()

  override fun getItem(position: Int): BirthdayListItem? {
    if (position < 0 || position >= itemCount) return null
    return super.getItem(position)
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
    return when (viewType) {
      AdsProvider.ADS_VIEW_TYPE ->
        BirthdayAdsViewHolder(parent, currentStateHolder, adsProvider, refreshListener)
      else ->
        BirthdayHolder(parent, currentStateHolder) { view, i, listActions ->
          actionsListener?.onAction(view, i, getItem(i), listActions)
        }
    }
  }

  override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
    if (holder is BirthdayHolder) {
      getItem(position)?.let { holder.setData(it) }
    }
  }

  override fun getItemViewType(position: Int): Int {
    val item = getItem(position)
    return if (!Module.isPro && item?.uuId == AdsProvider.BIRTHDAY_BANNER_ID) {
      AdsProvider.ADS_VIEW_TYPE
    } else {
      0
    }
  }

  fun onDestroy() {
    adsProvider.destroy()
  }
}
