package com.elementary.tasks.month_view

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.elementary.tasks.core.arch.BindingFragment
import com.elementary.tasks.core.calendar.EventsCursor
import com.elementary.tasks.core.data.models.Birthday
import com.elementary.tasks.core.data.ui.UiReminderListData
import com.elementary.tasks.core.utils.datetime.DateTimeManager
import com.elementary.tasks.core.utils.launchDefault
import com.elementary.tasks.core.utils.params.Prefs
import com.elementary.tasks.core.utils.withUIContext
import com.elementary.tasks.core.views.MonthView
import com.elementary.tasks.databinding.FragmentMonthViewBinding
import com.elementary.tasks.day_view.day.EventModel
import hirondelle.date4j.DateTime
import kotlinx.coroutines.delay
import org.koin.android.ext.android.inject
import org.threeten.bp.LocalDate
import timber.log.Timber

class MonthFragment : BindingFragment<FragmentMonthViewBinding>() {

  private val prefs by inject<Prefs>()
  private val dateTimeManager by inject<DateTimeManager>()

  private var callback: MonthCallback? = null
  private var mItem: MonthPagerItem? = null

  fun getModel(): MonthPagerItem? = mItem

  fun setModel(monthPagerItem: MonthPagerItem) {
    this.mItem = monthPagerItem
    Timber.d("setModel: $monthPagerItem, $isAdded, $isResumed")
    if (isResumed) {
      binding.monthView.setDate(monthPagerItem.year, monthPagerItem.monthValue)
    }
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    val fragment = parentFragment
    if (fragment != null) {
      callback = fragment as MonthCallback?
    }
    if (arguments != null) {
      mItem = arguments?.getParcelable(ARGUMENT_PAGE_NUMBER) as MonthPagerItem?
    }
  }

  override fun inflate(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ) = FragmentMonthViewBinding.inflate(inflater, container, false)

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    binding.monthView.setTodayColor(prefs.todayColor)
    binding.monthView.setStartDayOfWeek(prefs.startDay)

    binding.monthView.setDateClick(object : MonthView.OnDateClick {
      override fun onClick(date: LocalDate) {
        callback?.onDateClick(date)
      }
    })
    binding.monthView.setDateLongClick(object : MonthView.OnDateLongClick {
      override fun onLongClick(date: LocalDate) {
        callback?.onDateLongClick(date)
      }
    })
    mItem?.let {
      binding.monthView.setDate(it.year, it.monthValue)
    }
  }

  fun requestData() {
    val item = mItem
    if (item != null) {
      launchDefault {
        delay(250)
        withUIContext {
          val birthdayColor = callback?.birthdayColor() ?: Color.GREEN
          val reminderColor = callback?.reminderColor() ?: Color.BLUE
          callback?.find(item) { eventsPagerItem, list ->
            Timber.d("requestData: result -> $eventsPagerItem, ${list.size}")
            launchDefault {
              val data = mapData(list, birthdayColor, reminderColor)
              withUIContext {
                if (isResumed) binding.monthView.setEventsMap(data)
              }
            }
          }
        }
      }
    }
  }

  private fun mapData(list: List<EventModel>, birthdayColor: Int, reminderColor: Int): Map<DateTime, EventsCursor> {
    val map = mutableMapOf<DateTime, EventsCursor>()
    for (model in list) {
      val obj = model.model
      if (obj is Birthday) {
        var date = dateTimeManager.parseBirthdayDate(obj.date)
        val year = LocalDate.now().year
        if (date != null) {
          var i = -1
          while (i < 2) {
            date = date?.withYear(year + 1)
            date?.also { setEvent(it, obj.name, birthdayColor, EventsCursor.Type.BIRTHDAY, map) }
            i++
          }
        }
      } else if (obj is UiReminderListData) {
        val eventTime = obj.due?.localDateTime ?: continue
        setEvent(eventTime.toLocalDate(), obj.summary, reminderColor, EventsCursor.Type.REMINDER, map)
      }
    }
    Timber.d("mapData: $map")
    return map
  }

  private fun setEvent(
    date: LocalDate,
    summary: String,
    color: Int,
    type: EventsCursor.Type,
    map: MutableMap<DateTime, EventsCursor>
  ) {
    val key = DateTime(date.year, date.monthValue, date.dayOfMonth, 12, 0, 0, 0)
    if (map.containsKey(key)) {
      val eventsCursor = map[key] ?: EventsCursor()
      eventsCursor.addEvent(summary, color, type, date)
      map[key] = eventsCursor
    } else {
      val eventsCursor = EventsCursor(summary, color, type, date)
      map[key] = eventsCursor
    }
  }

  companion object {
    private const val ARGUMENT_PAGE_NUMBER = "arg_page"
    fun newInstance(item: MonthPagerItem): MonthFragment {
      val pageFragment = MonthFragment()
      val bundle = Bundle()
      bundle.putParcelable(ARGUMENT_PAGE_NUMBER, item)
      pageFragment.arguments = bundle
      return pageFragment
    }
  }
}
