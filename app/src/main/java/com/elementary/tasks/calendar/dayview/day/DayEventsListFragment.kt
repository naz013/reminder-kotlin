package com.elementary.tasks.calendar.dayview.day

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.elementary.tasks.R
import com.elementary.tasks.birthdays.BirthdayResolver
import com.elementary.tasks.calendar.data.BirthdayEventModel
import com.elementary.tasks.calendar.data.EventModel
import com.elementary.tasks.calendar.data.ReminderEventModel
import com.elementary.tasks.calendar.dayview.DayPagerItem
import com.elementary.tasks.core.arch.BindingFragment
import com.elementary.tasks.core.interfaces.ActionsListener
import com.elementary.tasks.core.utils.ListActions
import com.github.naz013.ui.common.theme.ThemeProvider
import com.github.naz013.feature.common.livedata.nonNullObserve
import com.github.naz013.ui.common.view.visibleGone
import com.elementary.tasks.databinding.FragmentEventsListBinding
import com.elementary.tasks.reminder.ReminderBuilderLauncher
import com.elementary.tasks.reminder.ReminderResolver
import com.github.naz013.logging.Logger
import org.koin.android.ext.android.inject

class DayEventsListFragment : BindingFragment<FragmentEventsListBinding>() {

  private val themeProvider by inject<ThemeProvider>()
  private val viewModel by inject<DayViewModel>()
  private val reminderBuilderLauncher by inject<ReminderBuilderLauncher>()

  private val dayEventsAdapter = DayEventsAdapter(isDark = themeProvider.isDark)
  private val birthdayResolver = BirthdayResolver(
    dialogAction = { dialogues },
    deleteAction = { birthday -> viewModel.deleteBirthday(birthday.uuId) }
  )
  private val reminderResolver = ReminderResolver(
    dialogAction = { dialogues },
    reminderBuilderLauncher = reminderBuilderLauncher,
    toggleAction = { },
    deleteAction = { reminder -> viewModel.moveToTrash(reminder) },
    skipAction = { reminder -> viewModel.skip(reminder) }
  )
  private var dayPagerItem: DayPagerItem? = null

  fun getModel(): DayPagerItem? = dayPagerItem

  fun setModel(dayPagerItem: DayPagerItem) {
    dayEventsAdapter.setData(listOf())
    this.dayPagerItem = dayPagerItem
    viewModel.onDateSelected(dayPagerItem.date)
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
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
    dayEventsAdapter.setEventListener(object : ActionsListener<EventModel> {
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
    binding.recyclerView.adapter = dayEventsAdapter

    reloadView()

    viewModel.events.nonNullObserve(viewLifecycleOwner) {
      Logger.d("nonNullObserve: $dayPagerItem, ${it.size}")
      dayEventsAdapter.setData(it)
      reloadView()
    }
  }

  private fun reloadView() {
    binding.recyclerView.visibleGone(dayEventsAdapter.itemCount > 0)
    binding.emptyItem.visibleGone(dayEventsAdapter.itemCount <= 0)
  }

  companion object {
    private const val ARGUMENT_PAGE_NUMBER = "arg_page"
    fun newInstance(item: DayPagerItem): DayEventsListFragment {
      val pageFragment = DayEventsListFragment()
      val bundle = Bundle()
      bundle.putParcelable(ARGUMENT_PAGE_NUMBER, item)
      pageFragment.arguments = bundle
      return pageFragment
    }
  }
}
