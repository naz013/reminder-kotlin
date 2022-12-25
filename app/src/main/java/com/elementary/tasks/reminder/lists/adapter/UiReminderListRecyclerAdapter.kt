package com.elementary.tasks.reminder.lists.adapter

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.elementary.tasks.AdsProvider
import com.elementary.tasks.core.data.ui.UiReminderList
import com.elementary.tasks.core.data.ui.UiReminderListActive
import com.elementary.tasks.core.data.ui.UiReminderListActiveGps
import com.elementary.tasks.core.data.ui.UiReminderListActiveShop
import com.elementary.tasks.core.data.ui.UiReminderListAds
import com.elementary.tasks.core.data.ui.UiReminderListData
import com.elementary.tasks.core.data.ui.UiReminderListRemoved
import com.elementary.tasks.core.data.ui.UiReminderListRemovedGps
import com.elementary.tasks.core.data.ui.UiReminderListRemovedShop
import com.elementary.tasks.core.data.ui.reminder.UiReminderViewType
import com.elementary.tasks.core.interfaces.ActionsListener
import com.elementary.tasks.core.utils.ListActions

class UiReminderListRecyclerAdapter(
  private val isDark: Boolean,
  private val isEditable: Boolean = true,
  private val refreshListener: () -> Unit
) : ListAdapter<UiReminderList, BaseUiReminderListViewHolder<*, *>>(
  UiReminderListDiffCallback()
) {

  var actionsListener: ActionsListener<UiReminderListData>? = null
  var data = listOf<UiReminderList>()
    private set
  private val adsProvider = AdsProvider()

  init {
    registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
      override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
        super.onItemRangeInserted(positionStart, itemCount)
        updateItem(positionStart + 1)
      }

      override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) {
        super.onItemRangeRemoved(positionStart, itemCount)
        updateItem(positionStart)
      }

      override fun onItemRangeMoved(fromPosition: Int, toPosition: Int, itemCount: Int) {
        super.onItemRangeMoved(fromPosition, toPosition, itemCount)
        updateItem(toPosition)
        updateItem(fromPosition)
      }
    })
  }

  private fun updateItem(position: Int) {
    if (itemCount > position) {
      notifyItemChanged(position)
      updateBefore(position)
      updateAfter(position, itemCount)
    }
  }

  private fun updateBefore(position: Int) {
    if (position > 0) {
      notifyItemChanged(position - 1)
    }
  }

  private fun updateAfter(position: Int, count: Int) {
    if (position + 1 < count) {
      notifyItemChanged(position + 1)
    }
  }

  override fun submitList(list: List<UiReminderList>?) {
    super.submitList(list)
    data = list ?: emptyList()
  }

  override fun onCreateViewHolder(
    parent: ViewGroup,
    viewType: Int
  ): BaseUiReminderListViewHolder<*, *> {
    val listener: (View, Int, ListActions) -> Unit = { view, i, listActions ->
      actionsListener?.onAction(view, i, find(i), listActions)
    }
    return when (viewType) {
      UiReminderViewType.GPS_ACTIVE.value -> {
        GpsViewHolder(parent, isEditable, showMore = true, listener)
      }
      UiReminderViewType.ACTIVE.value -> {
        ReminderViewHolder(parent, isEditable, showMore = true, listener)
      }
      UiReminderViewType.SHOPPING_ACTIVE.value -> {
        ShoppingViewHolder(parent, isEditable, showMore = true, isDark = isDark, listener)
      }
      UiReminderViewType.GPS_REMOVED.value -> {
        ArchivedGpsViewHolder(parent, isEditable, showMore = true, listener)
      }
      UiReminderViewType.SHOPPING_REMOVED.value -> {
        ArchivedShoppingViewHolder(parent, showMore = true, isDark = isDark, listener)
      }
      UiReminderViewType.REMOVED.value -> {
        ArchivedReminderViewHolder(parent, showMore = true, listener)
      }
      UiReminderViewType.ADS.value -> {
        ReminderAdsViewHolder(parent, adsProvider, refreshListener)
      }
      else -> {
        ReminderViewHolder(parent, isEditable, showMore = true, listener)
      }
    }
  }

  private fun find(position: Int): UiReminderListData? {
    if (position != -1 && position < itemCount) {
      return try {
        getItem(position).takeIf { it is UiReminderListData }?.let { it as UiReminderListData }
      } catch (e: Throwable) {
        null
      }
    }
    return null
  }

  override fun onBindViewHolder(
    holder: BaseUiReminderListViewHolder<*, *>,
    position: Int
  ) {
    val item = getItem(position)
    when (holder) {
      is GpsViewHolder -> {
        holder.setData(item as UiReminderListActiveGps)
      }
      is ArchivedGpsViewHolder -> {
        holder.setData(item as UiReminderListRemovedGps)
      }
      is ShoppingViewHolder -> {
        holder.setData(item as UiReminderListActiveShop)
      }
      is ArchivedShoppingViewHolder -> {
        holder.setData(item as UiReminderListRemovedShop)
      }
      is ReminderViewHolder -> {
        holder.setData(item as UiReminderListActive)
      }
      is ArchivedReminderViewHolder -> {
        holder.setData(item as UiReminderListRemoved)
      }
      else -> {
      }
    }
  }

  override fun getItemViewType(position: Int): Int {
    return when (getItem(position)) {
      is UiReminderListActive -> UiReminderViewType.ACTIVE
      is UiReminderListRemoved -> UiReminderViewType.REMOVED
      is UiReminderListRemovedShop -> UiReminderViewType.SHOPPING_REMOVED
      is UiReminderListActiveShop -> UiReminderViewType.SHOPPING_ACTIVE
      is UiReminderListActiveGps -> UiReminderViewType.GPS_ACTIVE
      is UiReminderListRemovedGps -> UiReminderViewType.GPS_REMOVED
      is UiReminderListAds -> UiReminderViewType.ADS
    }.value
  }

  fun onDestroy() {
    adsProvider.destroy()
  }
}
