package com.elementary.tasks.monthview

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.elementary.tasks.core.binding.HolderBinding
import com.elementary.tasks.core.calendar.EventsCursor
import com.elementary.tasks.core.utils.inflater
import com.elementary.tasks.core.views.MonthView
import com.elementary.tasks.databinding.FragmentMonthViewBinding
import hirondelle.date4j.DateTime
import org.threeten.bp.LocalDate
import timber.log.Timber

class InfinitePagerAdapter2(
  private val todayColor: Int,
  private val startDay: Int,
  private val monthCallback: MonthCallback
) : RecyclerView.Adapter<InfinitePagerAdapter2.ViewHolder>() {

  private var leftPart: List<MonthPagerItem> = emptyList()
  private var rightPart: List<MonthPagerItem> = emptyList()
  private var mapData: Map<DateTime, EventsCursor>? = null
  private var selectedPosition: Int = 1
  private var recyclerView: RecyclerView? = null

  fun selectPosition(position: Int) {
    selectedPosition = position
    val viewHolder = findViewHolder(position)
    Timber.d("selectPosition: $position, $viewHolder")
    if (viewHolder is ViewHolder) {
      onBindViewHolder(viewHolder, position)
    }
  }

  fun updateMapData(data: Map<DateTime, EventsCursor>) {
    this.mapData = data
    val viewHolder = findViewHolder(selectedPosition)
    Timber.d("selectPosition: $selectedPosition, $viewHolder")
    if (viewHolder is ViewHolder) {
      onBindViewHolder(viewHolder, selectedPosition)
    }
  }

  fun updateLeftSide(data: List<MonthPagerItem>) {
    this.leftPart = data
  }

  fun updateRightSide(data: List<MonthPagerItem>) {
    this.rightPart = data
  }

  override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
    super.onAttachedToRecyclerView(recyclerView)
    this.recyclerView = recyclerView

  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
    return ViewHolder(parent)
  }

  override fun onBindViewHolder(holder: ViewHolder, position: Int) {
    if (position > 2) {
      holder.bind(rightPart[position - 3], mapData?.takeIf { position == selectedPosition })
    } else {
      holder.bind(leftPart[position], mapData?.takeIf { position == selectedPosition })
    }
  }

  override fun onViewRecycled(holder: ViewHolder) {
    super.onViewRecycled(holder)
    Timber.d("onViewRecycled: ${holder.bindingAdapterPosition}")
  }

  override fun getItemCount(): Int {
    return 6
  }

  private fun findViewHolder(position: Int): RecyclerView.ViewHolder? {
    return recyclerView?.findViewHolderForAdapterPosition(position)
  }

  inner class ViewHolder(parent: ViewGroup) : HolderBinding<FragmentMonthViewBinding>(
    FragmentMonthViewBinding.inflate(parent.inflater(), parent, false)
  ) {

    fun bind(monthPagerItem: MonthPagerItem, data: Map<DateTime, EventsCursor>?) {
      Timber.d("bind: $bindingAdapterPosition, $monthPagerItem, $data")

      binding.monthView.setTodayColor(todayColor)
      binding.monthView.setStartDayOfWeek(startDay)

      binding.monthView.setDateClick(object : MonthView.OnDateClick {
        override fun onClick(date: LocalDate) {
          monthCallback.onDateClick(date)
        }
      })
      binding.monthView.setDateLongClick(object : MonthView.OnDateLongClick {
        override fun onLongClick(date: LocalDate) {
          monthCallback.onDateLongClick(date)
        }
      })
      binding.monthView.setDate(monthPagerItem.year, monthPagerItem.monthValue)
      data?.also { binding.monthView.setEventsMap(it) }
        ?: run { binding.monthView.setEventsMap(emptyMap()) }
    }
  }
}
