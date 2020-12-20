package com.elementary.tasks.reminder.lists.adapter

import android.app.AlarmManager
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.elementary.tasks.AdsProvider
import com.elementary.tasks.R
import com.elementary.tasks.core.arch.CurrentStateHolder
import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.core.interfaces.ActionsListener
import com.elementary.tasks.core.utils.Module
import com.elementary.tasks.core.utils.TimeUtil

class RemindersRecyclerAdapter(
  private val currentStateHolder: CurrentStateHolder,
  private val showHeader: Boolean = true,
  private val isEditable: Boolean = true,
  private val refreshListener: () -> Unit
) : ListAdapter<Reminder, RecyclerView.ViewHolder>(ReminderDiffCallback()) {

  var actionsListener: ActionsListener<Reminder>? = null
  var data = listOf<Reminder>()
    private set
  private val lang = currentStateHolder.preferences.appLanguage
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

  override fun submitList(list: List<Reminder>?) {
    super.submitList(list)
    data = list ?: emptyList()
    notifyDataSetChanged()
  }

  private fun initLabel(listHeader: TextView, position: Int) {
    val item = getItem(position)
    val due = TimeUtil.getDateTimeFromGmt(item.eventTime)
    var simpleDate = TimeUtil.getSimpleDate(due, lang)
    val prevItem: Reminder? = try {
      getItem(position - 1)
        ?.let {
          if (it.uuId == AdsProvider.REMINDER_BANNER_ID) {
            getItem(position - 2)
          } else it
        }
    } catch (e: Exception) {
      null
    }

    val context = listHeader.context
    if (!item.isActive && position > 0 && prevItem != null && prevItem.isActive) {
      simpleDate = context.getString(R.string.disabled)
      listHeader.text = simpleDate
      listHeader.visibility = View.VISIBLE
    } else if (!item.isActive && position > 0 && prevItem != null && !prevItem.isActive) {
      listHeader.visibility = View.GONE
    } else if (!item.isActive && position == 0) {
      simpleDate = context.getString(R.string.disabled)
      listHeader.text = simpleDate
      listHeader.visibility = View.VISIBLE
    } else if (item.isActive && position > 0 && prevItem != null &&
      simpleDate == TimeUtil.getSimpleDate(prevItem.eventTime, lang)) {
      listHeader.visibility = View.GONE
    } else {
      if (due <= 0 || due < System.currentTimeMillis() - AlarmManager.INTERVAL_DAY) {
        simpleDate = context.getString(R.string.permanent)
      } else {
        if (simpleDate == TimeUtil.getSimpleDate(System.currentTimeMillis(), lang)) {
          simpleDate = context.getString(R.string.today)
        } else if (simpleDate == TimeUtil.getSimpleDate(System.currentTimeMillis() + AlarmManager.INTERVAL_DAY, lang)) {
          simpleDate = context.getString(R.string.tomorrow)
        }
      }
      listHeader.text = simpleDate
      listHeader.visibility = View.VISIBLE
    }
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
    return when (viewType) {
      Reminder.REMINDER -> ReminderViewHolder(parent, currentStateHolder, true, isEditable, true) { view, i, listActions ->
        actionsListener?.onAction(view, i, find(i), listActions)
      }
      AdsProvider.ADS_VIEW_TYPE -> ReminderAdsViewHolder(parent, adsProvider, currentStateHolder, refreshListener)
      else -> ShoppingViewHolder(parent, currentStateHolder, isEditable, true) { view, i, listActions ->
        actionsListener?.onAction(view, i, find(i), listActions)
      }
    }
  }

  private fun find(position: Int): Reminder? {
    if (position != -1 && position < itemCount) {
      return try {
        getItem(position)
      } catch (e: Exception) {
        null
      }
    }
    return null
  }

  override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
    val item = getItem(position)
    if (holder is ReminderViewHolder) {
      holder.setData(item)
      if (showHeader) {
        initLabel(holder.listHeader, position)
      } else {
        holder.listHeader.visibility = View.GONE
      }
    } else if (holder is ShoppingViewHolder) {
      holder.setData(item)
      if (showHeader) {
        initLabel(holder.listHeader, position)
      } else {
        holder.listHeader.visibility = View.GONE
      }
    }
  }

  override fun getItemViewType(position: Int): Int {
    val item = getItem(position)
    return if (!Module.isPro && item.uuId == AdsProvider.REMINDER_BANNER_ID) {
      AdsProvider.ADS_VIEW_TYPE
    } else {
      item.viewType
    }
  }

  override fun getItemId(position: Int): Long {
    val item = getItem(position)
    return item.uniqueId.toLong()
  }

  fun onDestroy() {
    adsProvider.destroy()
  }
}
