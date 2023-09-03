package com.elementary.tasks.calendar.dayview.day

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.elementary.tasks.R
import com.elementary.tasks.birthdays.BirthdayResolver
import com.elementary.tasks.calendar.BirthdayEventModel
import com.elementary.tasks.calendar.EventModel
import com.elementary.tasks.calendar.ReminderEventModel
import com.elementary.tasks.calendar.dayview.DayPagerItem
import com.elementary.tasks.core.arch.BindingFragment
import com.elementary.tasks.core.interfaces.ActionsListener
import com.elementary.tasks.core.utils.ListActions
import com.elementary.tasks.core.utils.ThemeProvider
import com.elementary.tasks.core.utils.visibleGone
import com.elementary.tasks.databinding.FragmentEventsListBinding
import com.elementary.tasks.reminder.ReminderResolver
import org.koin.android.ext.android.inject
import timber.log.Timber

class EventsListFragment : BindingFragment<FragmentEventsListBinding>() {

  private val themeProvider by inject<ThemeProvider>()

  private val calendarEventsAdapter = CalendarEventsAdapter(isDark = themeProvider.isDark)
  private val birthdayResolver = BirthdayResolver(
    dialogAction = { dialogues },
    deleteAction = { birthday -> callback?.getViewModel()?.deleteBirthday(birthday.uuId) }
  )
  private val reminderResolver = ReminderResolver(
    dialogAction = { dialogues },
    toggleAction = { },
    deleteAction = { reminder -> callback?.getViewModel()?.moveToTrash(reminder) },
    skipAction = { reminder -> callback?.getViewModel()?.skip(reminder) }
  )
  private var dayPagerItem: DayPagerItem? = null
  private var callback: DayCallback? = null

  fun getModel(): DayPagerItem? = dayPagerItem

  fun setModel(dayPagerItem: DayPagerItem) {
    calendarEventsAdapter.setData(listOf())
    this.dayPagerItem = dayPagerItem
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    val fragment = parentFragment
    if (fragment != null) {
      callback = fragment as DayCallback?
    }
    if (arguments != null) {
      dayPagerItem = arguments?.getParcelable(ARGUMENT_PAGE_NUMBER) as DayPagerItem?
    }
  }

  override fun inflate(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ) = FragmentEventsListBinding.inflate(inflater, container, false)

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    calendarEventsAdapter.setEventListener(object : ActionsListener<EventModel> {
      override fun onAction(view: View, position: Int, t: EventModel?, actions: ListActions) {
        if (t == null) return
        when (t) {
          is BirthdayEventModel -> {
            birthdayResolver.resolveAction(view, t.model, actions)
          }
          is ReminderEventModel -> {
            reminderResolver.resolveAction(view, t.model, actions)
          }
        }
      }
    })
    if (resources.getBoolean(R.bool.is_tablet)) {
      binding.recyclerView.layoutManager = StaggeredGridLayoutManager(
        resources.getInteger(R.integer.num_of_cols),
        StaggeredGridLayoutManager.VERTICAL
      )
    } else {
      binding.recyclerView.layoutManager = LinearLayoutManager(context)
    }
    binding.recyclerView.adapter = calendarEventsAdapter

    reloadView()
    requestData()
  }

  private fun reloadView() {
    binding.recyclerView.visibleGone(calendarEventsAdapter.itemCount > 0)
    binding.emptyItem.visibleGone(calendarEventsAdapter.itemCount <= 0)
  }

  fun requestData() {
    Timber.d("requestData: $dayPagerItem, $callback")
    dayPagerItem?.also { dayPagerItem ->
      callback?.getViewModel()?.findEvents(dayPagerItem) { events ->
        Timber.d("requestData: $dayPagerItem, ${events.size}")
        calendarEventsAdapter.setData(events)
        reloadView()
      }
    }
  }

  companion object {
    private const val ARGUMENT_PAGE_NUMBER = "arg_page"
    fun newInstance(item: DayPagerItem): EventsListFragment {
      val pageFragment = EventsListFragment()
      val bundle = Bundle()
      bundle.putParcelable(ARGUMENT_PAGE_NUMBER, item)
      pageFragment.arguments = bundle
      return pageFragment
    }
  }
}