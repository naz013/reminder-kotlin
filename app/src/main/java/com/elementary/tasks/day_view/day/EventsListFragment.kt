package com.elementary.tasks.day_view.day

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.elementary.tasks.R
import com.elementary.tasks.birthdays.BirthdayResolver
import com.elementary.tasks.birthdays.list.BirthdayListItem
import com.elementary.tasks.core.arch.BindingFragment
import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.core.interfaces.ActionsListener
import com.elementary.tasks.core.utils.Dialogues
import com.elementary.tasks.core.utils.ListActions
import com.elementary.tasks.core.utils.launchDefault
import com.elementary.tasks.core.utils.withUIContext
import com.elementary.tasks.databinding.FragmentEventsListBinding
import com.elementary.tasks.day_view.EventsPagerItem
import com.elementary.tasks.reminder.ReminderResolver
import kotlinx.coroutines.delay
import org.koin.android.ext.android.get
import org.koin.android.ext.android.inject
import timber.log.Timber

class EventsListFragment : BindingFragment<FragmentEventsListBinding>() {

  private val dialogues by inject<Dialogues>()
  private val mAdapter = CalendarEventsAdapter(get())
  private val birthdayResolver = BirthdayResolver(
    dialogAction = { dialogues },
    deleteAction = { birthday -> callback?.getViewModel()?.deleteBirthday(birthday.uuId) }
  )
  private val reminderResolver = ReminderResolver(
    dialogAction = { dialogues },
    saveAction = { reminder -> callback?.getViewModel()?.saveReminder(reminder) },
    toggleAction = { },
    deleteAction = { reminder -> callback?.getViewModel()?.moveToTrash(reminder) },
    skipAction = { reminder -> callback?.getViewModel()?.skip(reminder) },
    allGroups = { callback?.getViewModel()?.groups ?: listOf() }
  )
  private var mItem: EventsPagerItem? = null
  private var callback: DayCallback? = null

  fun getModel(): EventsPagerItem? = mItem

  fun setModel(eventsPagerItem: EventsPagerItem) {
    mAdapter.setData(listOf())
    this.mItem = eventsPagerItem
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    val fragment = parentFragment
    if (fragment != null) {
      callback = fragment as DayCallback?
    }
    if (arguments != null) {
      mItem = arguments?.getParcelable(ARGUMENT_PAGE_NUMBER) as EventsPagerItem?
    }
  }

  override fun inflate(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ) = FragmentEventsListBinding.inflate(inflater, container, false)

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    mAdapter.setEventListener(object : ActionsListener<EventModel> {
      override fun onAction(view: View, position: Int, t: EventModel?, actions: ListActions) {
        if (t == null) return
        val item = t.model
        if (item is BirthdayListItem) {
          birthdayResolver.resolveAction(view, item, actions)
        } else if (item is Reminder) {
          reminderResolver.resolveAction(view, item, actions)
        }
      }
    })
    if (resources.getBoolean(R.bool.is_tablet)) {
      binding.recyclerView.layoutManager = StaggeredGridLayoutManager(resources.getInteger(R.integer.num_of_cols), StaggeredGridLayoutManager.VERTICAL)
    } else {
      binding.recyclerView.layoutManager = LinearLayoutManager(context)
    }
    binding.recyclerView.adapter = mAdapter

    reloadView()
  }

  private fun reloadView() {
    if (mAdapter.itemCount > 0) {
      binding.recyclerView.visibility = View.VISIBLE
      binding.emptyItem.visibility = View.GONE
    } else {
      binding.recyclerView.visibility = View.GONE
      binding.emptyItem.visibility = View.VISIBLE
    }
  }

  fun requestData() {
    val item = mItem
    if (item != null) {
      launchDefault {
        delay(250)
        withUIContext {
          callback?.find(item) { eventsPagerItem, list ->
            Timber.d("setModel: $eventsPagerItem, ${list.size}")
            mAdapter.setData(list)
            reloadView()
          }
        }
      }

    }
  }

  companion object {
    private const val ARGUMENT_PAGE_NUMBER = "arg_page"
    fun newInstance(item: EventsPagerItem): EventsListFragment {
      val pageFragment = EventsListFragment()
      val bundle = Bundle()
      bundle.putParcelable(ARGUMENT_PAGE_NUMBER, item)
      pageFragment.arguments = bundle
      return pageFragment
    }
  }
}
