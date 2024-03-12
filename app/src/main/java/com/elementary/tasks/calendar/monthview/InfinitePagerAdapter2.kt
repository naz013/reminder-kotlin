package com.elementary.tasks.calendar.monthview

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.elementary.tasks.calendar.data.MonthLiveData
import com.elementary.tasks.core.binding.HolderBinding
import com.elementary.tasks.core.protocol.StartDayOfWeekProtocol
import com.elementary.tasks.core.utils.ui.inflater
import com.elementary.tasks.core.views.MonthView
import com.elementary.tasks.databinding.FragmentMonthViewBinding
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.threeten.bp.LocalDate
import timber.log.Timber

class InfinitePagerAdapter2(
  private val dataAccessor: DataAccessor,
  private val monthCallback: MonthCallback
) : RecyclerView.Adapter<InfinitePagerAdapter2.ViewHolderDynamic>() {

  private var leftPart: List<MonthPagerItem> = emptyList()
  private var rightPart: List<MonthPagerItem> = emptyList()
  private var selectedPosition: Int = 1
  private var recyclerView: RecyclerView? = null

  fun selectPosition(position: Int) {
    selectedPosition = position
    val viewHolder = findViewHolder(position)
    Timber.d("selectPosition: $position, $viewHolder")
    if (viewHolder is ViewHolderDynamic) {
      onBindViewHolder(viewHolder, position)
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

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolderDynamic {
    return ViewHolderDynamic(parent)
  }

  override fun onBindViewHolder(holder: ViewHolderDynamic, position: Int) {
    if (position > 2) {
      holder.bind(rightPart[position - 3])
    } else {
      holder.bind(leftPart[position])
    }
  }

  override fun onViewRecycled(holder: ViewHolderDynamic) {
    super.onViewRecycled(holder)
    Timber.d("onViewRecycled: ${holder.bindingAdapterPosition}")
  }

  override fun getItemCount(): Int {
    return 6
  }

  private fun findViewHolder(position: Int): RecyclerView.ViewHolder? {
    return recyclerView?.findViewHolderForAdapterPosition(position)
  }

  inner class ViewHolderDynamic(parent: ViewGroup) :
    HolderBinding<FragmentMonthViewBinding>(
      FragmentMonthViewBinding.inflate(parent.inflater(), parent, false)
    ),
    KoinComponent {

    private val monthLiveData by inject<MonthLiveData>()

    init {
      monthLiveData.observeForever {
        Timber.d("onChanged: map=${it.size}")
        binding.monthView.setEventsMap(it)
      }
    }

    fun bind(monthPagerItem: MonthPagerItem) {
      Timber.d("bind: $bindingAdapterPosition, $monthPagerItem")

      binding.monthView.setTodayColor(dataAccessor.getTodayColor())
      binding.monthView.setStartDayOfWeek(dataAccessor.getStartDay())

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

      monthLiveData.onDateChanged(monthPagerItem.date)
    }
  }

  interface DataAccessor {
    fun getTodayColor(): Int
    fun getStartDay(): StartDayOfWeekProtocol
  }
}
