package com.elementary.tasks.calendar.dayview.day

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
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
import com.elementary.tasks.databinding.FragmentEventsListBinding
import com.elementary.tasks.reminder.ReminderResolver
import com.github.naz013.common.Module.isTablet
import com.github.naz013.common.intent.IntentKeys
import com.github.naz013.feature.common.android.readParcelable
import com.github.naz013.feature.common.livedata.nonNullObserve
import com.github.naz013.ui.common.theme.ThemeProvider
import com.github.naz013.ui.common.view.visibleGone
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf
import org.threeten.bp.LocalDate

class DayEventsListFragment : BindingFragment<FragmentEventsListBinding>() {

  private val themeProvider by inject<ThemeProvider>()
  private val viewModel by viewModel<DayViewModel> { parametersOf(getDate()) }

  private val dayEventsAdapter = DayEventsAdapter(
    isDark = themeProvider.isDark,
    eventListener = object : ActionsListener<EventModel> {
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
    }
  )
  private val birthdayResolver = BirthdayResolver(
    dialogAction = { dialogues },
    deleteAction = { birthday -> viewModel.deleteBirthday(birthday.uuId) },
    birthdayEditAction = {
      findNavController().navigate(
        R.id.editBirthdayFragment,
        Bundle().apply {
          putString(IntentKeys.INTENT_ID, it.uuId)
        }
      )
    },
    birthdayOpenAction = {
      findNavController().navigate(
        R.id.previewBirthdayFragment,
        Bundle().apply {
          putString(IntentKeys.INTENT_ID, it.uuId)
        }
      )
    }
  )
  private val reminderResolver = ReminderResolver(
    dialogAction = { dialogues },
    toggleAction = { },
    deleteAction = { reminder -> viewModel.moveToTrash(reminder) },
    skipAction = { reminder -> viewModel.skip(reminder) },
    openAction = {
      findNavController().navigate(
        R.id.previewReminderFragment,
        Bundle().apply {
          putString(IntentKeys.INTENT_ID, it.id)
        }
      )
    },
    editAction = {
      findNavController().navigate(
        R.id.buildReminderFragment,
        Bundle().apply {
          putString(IntentKeys.INTENT_ID, it.id)
        }
      )
    }
  )

  private fun getDate(): LocalDate {
    return arguments?.readParcelable(ARGUMENT_PAGE_NUMBER, DayPagerItem::class.java)
      ?.date
      ?: LocalDate.now()
  }

  override fun inflate(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ) = FragmentEventsListBinding.inflate(inflater, container, false)

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    if (isTablet()) {
      binding.recyclerView.layoutManager = StaggeredGridLayoutManager(
        resources.getInteger(R.integer.num_of_cols),
        StaggeredGridLayoutManager.VERTICAL
      )
    } else {
      binding.recyclerView.layoutManager = LinearLayoutManager(context)
    }
    binding.recyclerView.adapter = dayEventsAdapter

    reloadView()

    initViewModel()
  }

  private fun initViewModel() {
    lifecycle.addObserver(viewModel)
    viewModel.events.nonNullObserve(viewLifecycleOwner) {
      dayEventsAdapter.submitList(it)
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
